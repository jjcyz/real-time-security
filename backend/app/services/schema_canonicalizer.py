"""Schema canonicalization system for normalizing different file types."""

import pandas as pd
from typing import Dict, Any, Optional, List
from dataclasses import dataclass
import numpy as np


@dataclass
class FileMetadata:
    """Metadata about the uploaded file."""
    file_type: str
    detected_schema: Dict[str, str]  # original_field -> canonical_field
    confidence: float
    row_count: int
    has_timestamp: bool
    has_amount: bool
    has_user_id: bool
    risk_score: float = 0.0


class SchemaCanonicalizer:
    """Canonicalizes different CSV schemas into a standard format."""

    def __init__(self):
        self.canonical_fields = {
            'id': 'id',
            'timestamp': 'timestamp',
            'amount': 'amount',
            'merchant': 'merchant',
            'location': 'location',
            'user_id': 'user_id',
            'payment_method': 'payment_method',
            'ip_address': 'ip_address',
            'device_info': 'device_info',
            'currency': 'currency'
        }

        # Field mappings for different file types
        self.field_mappings = {
            'inventory': {
                'id': ['movement_id', 'transaction_id', 'id'],
                'timestamp': ['date', 'time', 'datetime', 'timestamp'],
                'amount': ['total_value', 'value', 'cost', 'amount', 'price'],
                'merchant': ['item_name', 'product', 'item_code', 'merchant'],
                'location': ['location', 'warehouse', 'store'],
                'user_id': ['employee_id', 'user', 'staff_id', 'user_id'],
                'quantity': ['quantity', 'qty', 'count']
            },
            'financial': {
                'id': ['transaction_id', 'id', 'txn_id'],
                'timestamp': ['timestamp', 'date', 'created_at', 'time'],
                'amount': ['amount', 'value', 'total', 'sum'],
                'merchant': ['merchant', 'store', 'vendor', 'business'],
                'location': ['location', 'address', 'city', 'country'],
                'user_id': ['user_id', 'customer_id', 'account_id', 'client_id'],
                'payment_method': ['payment_method', 'method', 'type', 'card_type'],
                'ip_address': ['ip_address', 'ip', 'client_ip'],
                'device_info': ['device_info', 'device', 'user_agent', 'browser']
            },
            'security_logs': {
                'id': ['log_id', 'event_id', 'id'],
                'timestamp': ['timestamp', 'time', 'datetime', 'created_at'],
                'user_id': ['user_id', 'username', 'user', 'account'],
                'ip_address': ['ip_address', 'ip', 'source_ip'],
                'device_info': ['user_agent', 'device', 'browser', 'os'],
                'location': ['location', 'country', 'city', 'region']
            }
        }

    def detect_file_type(self, df: pd.DataFrame) -> tuple[str, float]:
        """Detect file type based on column names and data patterns."""
        columns = [col.lower().strip() for col in df.columns]

        # Scoring system for file type detection
        scores = {
            'inventory': 0,
            'financial': 0,
            'security_logs': 0
        }

        # Inventory indicators
        inventory_indicators = ['item_code', 'movement_id', 'quantity', 'item_name', 'movement_type']
        for indicator in inventory_indicators:
            if any(indicator in col for col in columns):
                scores['inventory'] += 2

        # Financial indicators
        financial_indicators = ['transaction_id', 'payment_method', 'amount', 'merchant', 'currency']
        for indicator in financial_indicators:
            if any(indicator in col for col in columns):
                scores['financial'] += 2

        # Security log indicators
        security_indicators = ['log_id', 'event_id', 'user_agent', 'source_ip', 'action']
        for indicator in security_indicators:
            if any(indicator in col for col in columns):
                scores['security_logs'] += 2

        # Additional heuristics
        if any('out' in col for col in columns) and 'inventory' in scores:
            scores['inventory'] += 1  # Movement type suggests inventory

        if any('card' in col or 'payment' in col for col in columns):
            scores['financial'] += 1

        if any('login' in col or 'auth' in col for col in columns):
            scores['security_logs'] += 1

        # Return best match
        best_type = max(scores, key=scores.get)
        confidence = min(scores[best_type] / 10.0, 1.0)  # Normalize to 0-1

        return best_type, confidence

    def canonicalize_schema(self, df: pd.DataFrame) -> tuple[pd.DataFrame, FileMetadata]:
        """Convert file to canonical schema and return metadata."""
        original_df = df.copy()

        # Detect file type
        file_type, confidence = self.detect_file_type(df)

        # Get field mappings for this file type
        mappings = self.field_mappings.get(file_type, {})
        detected_schema = {}

        # Create canonical dataframe
        canonical_df = pd.DataFrame()

        # Map each canonical field
        for canonical_field, possible_fields in mappings.items():
            mapped_field = None
            for field in possible_fields:
                if field in df.columns:
                    canonical_df[canonical_field] = df[field]
                    detected_schema[field] = canonical_field
                    mapped_field = field
                    break

            if mapped_field is None:
                # Create empty column if no mapping found
                canonical_df[canonical_field] = None

        # Add any unmapped columns as raw data
        unmapped_cols = [col for col in df.columns if col not in detected_schema]
        for col in unmapped_cols:
            canonical_df[f"raw_{col}"] = df[col]

        # Calculate risk score based on data quality
        risk_score = self._calculate_risk_score(canonical_df, file_type)

        # Create metadata (convert numpy types to Python native types)
        metadata = FileMetadata(
            file_type=file_type,
            detected_schema=detected_schema,
            confidence=float(confidence),
            row_count=int(len(df)),
            has_timestamp=bool(canonical_df['timestamp'].notna().any()),
            has_amount=bool(canonical_df['amount'].notna().any()),
            has_user_id=bool(canonical_df['user_id'].notna().any()),
            risk_score=float(risk_score)
        )

        return canonical_df, metadata

    def _calculate_risk_score(self, df: pd.DataFrame, file_type: str) -> float:
        """Calculate initial risk score based on data characteristics."""
        risk_score = 0.0

        # Missing data penalty
        missing_data_ratio = df.isnull().sum().sum() / (len(df) * len(df.columns))
        risk_score += missing_data_ratio * 0.3

        # Data quality checks
        if 'amount' in df.columns and df['amount'].notna().any():
            # Check for negative amounts (potential data issues)
            negative_amounts = (df['amount'] < 0).sum()
            if negative_amounts > 0:
                risk_score += min(negative_amounts / len(df), 0.2)

            # Check for extremely high amounts (potential outliers)
            if df['amount'].max() > df['amount'].quantile(0.99) * 10:
                risk_score += 0.1

        # Timestamp quality
        if 'timestamp' in df.columns and df['timestamp'].notna().any():
            try:
                pd.to_datetime(df['timestamp'])
            except:
                risk_score += 0.2  # Invalid timestamps

        # File type specific risk factors
        if file_type == 'inventory':
            # Check for unusual movement patterns
            if 'quantity' in df.columns:
                if (df['quantity'] < 0).any():
                    risk_score += 0.1  # Negative quantities suspicious

        return min(risk_score, 1.0)  # Cap at 1.0


# Global canonicalizer instance
schema_canonicalizer = SchemaCanonicalizer()
