"""Fraud detection service with various detection rules."""

import pandas as pd
from typing import List, Dict, Any, Tuple
from datetime import datetime, timedelta
from sqlalchemy.orm import Session
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
import numpy as np

from ..models.transaction import Transaction
from ..models.alert import Alert, AlertSeverity, AlertStatus
from ..models.uploaded_file import UploadedFile


class FraudDetectionService:
    """Service for detecting fraudulent transactions using various rules."""

    def __init__(self, db: Session):
        self.db = db

    def analyze_file(self, file_id: int, rules: List[str] = None) -> Dict[str, Any]:
        """Analyze all transactions in a file for fraud."""
        start_time = datetime.now()

        # Get the uploaded file
        uploaded_file = self.db.query(UploadedFile).filter(UploadedFile.id == file_id).first()
        if not uploaded_file:
            raise ValueError(f"File with ID {file_id} not found")

        # Get all transactions for this file
        transactions = self.db.query(Transaction).filter(
            Transaction.uploaded_file_id == file_id
        ).all()

        if not transactions:
            return {
                "file_id": file_id,
                "total_transactions": 0,
                "suspicious_count": 0,
                "alerts_created": 0,
                "analysis_time": 0,
                "rules_applied": [],
                "summary": {}
            }

        # Convert to DataFrame for analysis
        df = self._transactions_to_dataframe(transactions)
        print(f"Created DataFrame with {len(df)} rows and columns: {list(df.columns)}")

        # Apply fraud detection rules
        if rules is None:
            rules = [
                "duplicate_transactions",
                "high_value_transactions",
                "off_hours_transactions",
                "rapid_successive_transactions",
                "anomaly_detection"
            ]

        print(f"Applying fraud detection rules: {rules}")
        suspicious_transactions = set()
        alerts_created = 0

        for rule in rules:
            print(f"\n--- Applying rule: {rule} ---")
            rule_suspicious, rule_alerts = self._apply_rule(rule, df, transactions, file_id)
            suspicious_transactions.update(rule_suspicious)
            alerts_created += rule_alerts
            print(f"Rule {rule}: {rule_alerts} alerts, {len(rule_suspicious)} suspicious transactions")

        # Update transaction records
        for transaction in transactions:
            if transaction.id in suspicious_transactions:
                transaction.is_suspicious = True
                transaction.risk_score = min(transaction.risk_score + 0.3, 1.0)

        # Update file status
        uploaded_file.status = "completed"
        uploaded_file.processed_rows = len(transactions)

        self.db.commit()

        analysis_time = (datetime.now() - start_time).total_seconds()

        return {
            "file_id": file_id,
            "total_transactions": len(transactions),
            "suspicious_count": len(suspicious_transactions),
            "alerts_created": alerts_created,
            "analysis_time": analysis_time,
            "rules_applied": rules,
            "summary": {
                "suspicious_percentage": len(suspicious_transactions) / len(transactions) * 100,
                "average_risk_score": np.mean([t.risk_score for t in transactions]),
                "high_risk_count": len([t for t in transactions if t.risk_score > 0.7])
            }
        }

    def _transactions_to_dataframe(self, transactions: List[Transaction]) -> pd.DataFrame:
        """Convert transactions to DataFrame for analysis."""
        data = []
        for t in transactions:
            data.append({
                "id": t.id,
                "amount": t.amount or 0,
                "timestamp": t.timestamp,
                "merchant": t.merchant or "",
                "location": t.location or "",
                "payment_method": t.payment_method or "",
                "user_id": t.user_id or "",
                "ip_address": t.ip_address or "",
                "device_info": t.device_info or ""
            })
        return pd.DataFrame(data)

    def _apply_rule(self, rule_name: str, df: pd.DataFrame, transactions: List[Transaction], file_id: int) -> Tuple[set, int]:
        """Apply a specific fraud detection rule."""
        if rule_name == "duplicate_transactions":
            return self._detect_duplicate_transactions(df, transactions, file_id)
        elif rule_name == "high_value_transactions":
            return self._detect_high_value_transactions(df, transactions, file_id)
        elif rule_name == "off_hours_transactions":
            return self._detect_off_hours_transactions(df, transactions, file_id)
        elif rule_name == "rapid_successive_transactions":
            return self._detect_rapid_successive_transactions(df, transactions, file_id)
        elif rule_name == "anomaly_detection":
            return self._detect_anomalies(df, transactions, file_id)
        else:
            return set(), 0

    def _detect_duplicate_transactions(self, df: pd.DataFrame, transactions: List[Transaction], file_id: int) -> Tuple[set, int]:
        """Detect duplicate transactions based on amount, merchant, and timestamp."""
        suspicious_ids = set()
        alerts_created = 0

        print(f"Detecting duplicate transactions in {len(df)} rows")

        # Convert timestamp to datetime and extract hour
        df['timestamp_dt'] = pd.to_datetime(df['timestamp'])
        df['hour'] = df['timestamp_dt'].dt.floor('H')

        # Group by amount, merchant, and hour
        duplicates = df.groupby(['amount', 'merchant', 'hour']).size()
        duplicate_groups = duplicates[duplicates > 1]

        print(f"Found {len(duplicate_groups)} duplicate groups")

        for (amount, merchant, hour), count in duplicate_groups.items():
            if count > 1:
                group_transactions = df[
                    (df['amount'] == amount) &
                    (df['merchant'] == merchant) &
                    (df['hour'] == hour)
                ]

                transaction_ids = group_transactions['id'].tolist()
                suspicious_ids.update(transaction_ids)

                print(f"Creating alert for {count} duplicate transactions at {merchant} for ${amount}")

                # Create alert
                alert = Alert(
                    title=f"Duplicate Transactions Detected",
                    description=f"Found {count} duplicate transactions at {merchant} for ${amount} within the same hour",
                    severity=AlertSeverity.MEDIUM,
                    rule_name="duplicate_transactions",
                    confidence_score=0.8,
                    risk_score=0.6,
                    transaction_ids=transaction_ids,
                    alert_metadata={
                        "merchant": merchant,
                        "amount": amount,
                        "hour": hour.isoformat(),
                        "count": count
                    }
                )
                self.db.add(alert)
                alerts_created += 1

        print(f"Duplicate detection: {alerts_created} alerts created, {len(suspicious_ids)} suspicious transactions")
        return suspicious_ids, alerts_created

    def _detect_high_value_transactions(self, df: pd.DataFrame, transactions: List[Transaction], file_id: int) -> Tuple[set, int]:
        """Detect unusually high-value transactions."""
        suspicious_ids = set()
        alerts_created = 0

        print(f"Detecting high-value transactions in {len(df)} rows")
        print(f"Amount range: ${df['amount'].min()} - ${df['amount'].max()}")

        # Calculate threshold (95th percentile)
        threshold = df['amount'].quantile(0.95)
        high_value_transactions = df[df['amount'] > threshold]

        print(f"High-value threshold: ${threshold:.2f}")
        print(f"Found {len(high_value_transactions)} high-value transactions")

        if len(high_value_transactions) > 0:
            transaction_ids = high_value_transactions['id'].tolist()
            suspicious_ids.update(transaction_ids)

            print(f"Creating alert for {len(high_value_transactions)} high-value transactions")

            # Create alert
            alert = Alert(
                title=f"High-Value Transactions Detected",
                description=f"Found {len(high_value_transactions)} transactions above ${threshold:.2f} threshold",
                severity=AlertSeverity.HIGH,
                rule_name="high_value_transactions",
                confidence_score=0.7,
                risk_score=0.8,
                transaction_ids=transaction_ids,
                alert_metadata={
                    "threshold": threshold,
                    "max_amount": df['amount'].max(),
                    "count": len(high_value_transactions)
                }
            )
            self.db.add(alert)
            alerts_created += 1

        print(f"High-value detection: {alerts_created} alerts created, {len(suspicious_ids)} suspicious transactions")
        return suspicious_ids, alerts_created

    def _detect_off_hours_transactions(self, df: pd.DataFrame, transactions: List[Transaction], file_id: int) -> Tuple[set, int]:
        """Detect transactions outside normal business hours."""
        suspicious_ids = set()
        alerts_created = 0

        # Convert timestamp to hour
        df['hour'] = pd.to_datetime(df['timestamp']).dt.hour

        # Define off-hours (11 PM to 6 AM)
        off_hours_transactions = df[(df['hour'] >= 23) | (df['hour'] <= 6)]

        if len(off_hours_transactions) > 0:
            transaction_ids = off_hours_transactions['id'].tolist()
            suspicious_ids.update(transaction_ids)

            # Create alert
            alert = Alert(
                title=f"Off-Hours Transactions Detected",
                description=f"Found {len(off_hours_transactions)} transactions outside normal business hours (11 PM - 6 AM)",
                severity=AlertSeverity.MEDIUM,
                rule_name="off_hours_transactions",
                confidence_score=0.6,
                risk_score=0.5,
                transaction_ids=transaction_ids,
                alert_metadata={
                    "count": len(off_hours_transactions),
                    "hours": off_hours_transactions['hour'].tolist()
                }
            )
            self.db.add(alert)
            alerts_created += 1

        return suspicious_ids, alerts_created

    def _detect_rapid_successive_transactions(self, df: pd.DataFrame, transactions: List[Transaction], file_id: int) -> Tuple[set, int]:
        """Detect rapid successive transactions from the same user."""
        suspicious_ids = set()
        alerts_created = 0

        # Sort by user_id and timestamp
        df_sorted = df.sort_values(['user_id', 'timestamp'])

        # Group by user and find rapid transactions (within 5 minutes)
        for user_id in df_sorted['user_id'].unique():
            if user_id == "":
                continue

            user_transactions = df_sorted[df_sorted['user_id'] == user_id]
            if len(user_transactions) < 2:
                continue

            # Calculate time differences
            user_transactions = user_transactions.copy()
            user_transactions['time_diff'] = pd.to_datetime(user_transactions['timestamp']).diff()

            # Find transactions within 5 minutes
            rapid_transactions = user_transactions[user_transactions['time_diff'] < timedelta(minutes=5)]

            if len(rapid_transactions) > 1:
                transaction_ids = rapid_transactions['id'].tolist()
                suspicious_ids.update(transaction_ids)

                # Create alert
                alert = Alert(
                    title=f"Rapid Successive Transactions Detected",
                    description=f"User {user_id} made {len(rapid_transactions)} transactions within 5 minutes",
                    severity=AlertSeverity.HIGH,
                    rule_name="rapid_successive_transactions",
                    confidence_score=0.8,
                    risk_score=0.7,
                    transaction_ids=transaction_ids,
                    user_ids=[user_id],
                    alert_metadata={
                        "user_id": user_id,
                        "count": len(rapid_transactions),
                        "time_span_minutes": 5
                    }
                )
                self.db.add(alert)
                alerts_created += 1

        return suspicious_ids, alerts_created

    def _detect_anomalies(self, df: pd.DataFrame, transactions: List[Transaction], file_id: int) -> Tuple[set, int]:
        """Detect anomalies using machine learning."""
        suspicious_ids = set()
        alerts_created = 0

        # Prepare features for anomaly detection
        features = ['amount']
        if 'hour' not in df.columns:
            df['hour'] = pd.to_datetime(df['timestamp']).dt.hour
        features.append('hour')

        # Add merchant encoding (simple hash)
        df['merchant_hash'] = df['merchant'].apply(lambda x: hash(x) % 1000)
        features.append('merchant_hash')

        # Prepare data
        X = df[features].fillna(0)

        if len(X) < 10:  # Need minimum samples for anomaly detection
            return suspicious_ids, alerts_created

        # Standardize features
        scaler = StandardScaler()
        X_scaled = scaler.fit_transform(X)

        # Apply Isolation Forest
        iso_forest = IsolationForest(contamination=0.1, random_state=42)
        anomaly_labels = iso_forest.fit_predict(X_scaled)

        # Get anomalous transactions
        anomalous_indices = df[anomaly_labels == -1].index
        if len(anomalous_indices) > 0:
            transaction_ids = df.loc[anomalous_indices, 'id'].tolist()
            suspicious_ids.update(transaction_ids)

            # Create alert
            alert = Alert(
                title=f"Anomalous Transactions Detected",
                description=f"Machine learning detected {len(anomalous_indices)} anomalous transaction patterns",
                severity=AlertSeverity.MEDIUM,
                rule_name="anomaly_detection",
                confidence_score=0.7,
                risk_score=0.6,
                transaction_ids=transaction_ids,
                alert_metadata={
                    "count": len(anomalous_indices),
                    "contamination": 0.1,
                    "algorithm": "IsolationForest"
                }
            )
            self.db.add(alert)
            alerts_created += 1

        return suspicious_ids, alerts_created
