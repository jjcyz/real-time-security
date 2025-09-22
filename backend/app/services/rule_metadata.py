"""Rule metadata system with predicates, costs, and tags for intelligent rule selection."""

from dataclasses import dataclass
from typing import List, Dict, Any, Callable, Optional
from enum import Enum
import pandas as pd
import numpy as np


class RuleCategory(str, Enum):
    """Rule categories for organization and filtering."""
    FINANCIAL = "financial"
    TEMPORAL = "temporal"
    BEHAVIORAL = "behavioral"
    INVENTORY = "inventory"
    NETWORK = "network"
    ML_ANOMALY = "ml_anomaly"


class RuleCost(str, Enum):
    """Rule execution cost levels."""
    CHEAP = "cheap"      # < 10ms, simple calculations
    MEDIUM = "medium"    # 10-100ms, moderate processing
    EXPENSIVE = "expensive"  # > 100ms, ML models, complex analysis


@dataclass
class RulePredicate:
    """Predicate that determines if a rule should run on given data."""
    name: str
    condition: Callable[[pd.DataFrame, Dict[str, Any]], bool]
    description: str


@dataclass
class RuleMetadata:
    """Metadata for a fraud detection rule."""
    name: str
    category: RuleCategory
    cost: RuleCost
    predicates: List[RulePredicate]
    tags: List[str]  # File types, data requirements, etc.
    priority: int  # 1-10, higher = more important
    description: str
    confidence_threshold: float = 0.5
    risk_threshold: float = 0.0


class RuleRegistry:
    """Registry for managing rule metadata and selection."""

    def __init__(self):
        self.rules: Dict[str, RuleMetadata] = {}
        self._register_default_rules()

    def register_rule(self, metadata: RuleMetadata):
        """Register a new rule with metadata."""
        self.rules[metadata.name] = metadata

    def get_applicable_rules(self, df: pd.DataFrame, file_metadata: Dict[str, Any]) -> List[str]:
        """Get rules that should run based on data and file characteristics."""
        applicable = []

        for rule_name, rule_meta in self.rules.items():
            # Check if all predicates pass
            predicate_results = []
            for predicate in rule_meta.predicates:
                try:
                    result = predicate.condition(df, file_metadata)
                    predicate_results.append(result)
                except Exception as e:
                    predicate_results.append(False)

            if all(predicate_results):
                applicable.append(rule_name)

        # Sort by priority (higher first), then by cost (cheaper first)
        applicable.sort(key=lambda name: (
            -self.rules[name].priority,
            self.rules[name].cost.value
        ))

        return applicable

    def get_rule_metadata(self, rule_name: str) -> Optional[RuleMetadata]:
        """Get metadata for a specific rule."""
        return self.rules.get(rule_name)

    def _register_default_rules(self):
        """Register default rules with their metadata."""

        # High-value transactions rule
        self.register_rule(RuleMetadata(
            name="high_value_transactions",
            category=RuleCategory.FINANCIAL,
            cost=RuleCost.CHEAP,
            predicates=[
                RulePredicate(
                    name="has_amount_field",
                    condition=lambda df, meta: 'amount' in df.columns and df['amount'].notna().any(),
                    description="Data must have amount field with values"
                ),
                RulePredicate(
                    name="sufficient_data",
                    condition=lambda df, meta: len(df) >= 10,
                    description="Need at least 10 transactions for statistical analysis"
                )
            ],
            tags=["financial", "amount", "statistical"],
            priority=8,
            description="Detect unusually high-value transactions using 95th percentile"
        ))

        # Duplicate transactions rule
        self.register_rule(RuleMetadata(
            name="duplicate_transactions",
            category=RuleCategory.FINANCIAL,
            cost=RuleCost.CHEAP,
            predicates=[
                RulePredicate(
                    name="has_amount_merchant",
                    condition=lambda df, meta: all(col in df.columns for col in ['amount', 'merchant']),
                    description="Need amount and merchant fields"
                ),
                RulePredicate(
                    name="has_timestamp",
                    condition=lambda df, meta: 'timestamp' in df.columns,
                    description="Need timestamp for time-based grouping"
                )
            ],
            tags=["financial", "duplicate", "merchant"],
            priority=7,
            description="Detect duplicate transactions by amount, merchant, and time"
        ))

        # Off-hours transactions rule
        self.register_rule(RuleMetadata(
            name="off_hours_transactions",
            category=RuleCategory.TEMPORAL,
            cost=RuleCost.CHEAP,
            predicates=[
                RulePredicate(
                    name="has_timestamp",
                    condition=lambda df, meta: 'timestamp' in df.columns,
                    description="Need timestamp for hour analysis"
                ),
                RulePredicate(
                    name="not_inventory_only",
                    condition=lambda df, meta: meta.get('file_type') != 'inventory',
                    description="Skip for inventory files (24/7 operations)"
                )
            ],
            tags=["temporal", "hours", "business"],
            priority=6,
            description="Detect transactions outside normal business hours"
        ))

        # Rapid successive transactions rule
        self.register_rule(RuleMetadata(
            name="rapid_successive_transactions",
            category=RuleCategory.BEHAVIORAL,
            cost=RuleCost.MEDIUM,
            predicates=[
                RulePredicate(
                    name="has_user_timestamp",
                    condition=lambda df, meta: all(col in df.columns for col in ['user_id', 'timestamp']),
                    description="Need user_id and timestamp for user behavior analysis"
                ),
                RulePredicate(
                    name="multiple_users",
                    condition=lambda df, meta: df['user_id'].nunique() > 1,
                    description="Need multiple users for pattern detection"
                )
            ],
            tags=["behavioral", "user", "velocity"],
            priority=7,
            description="Detect rapid successive transactions from same user"
        ))

        # ML Anomaly detection rule
        self.register_rule(RuleMetadata(
            name="anomaly_detection",
            category=RuleCategory.ML_ANOMALY,
            cost=RuleCost.EXPENSIVE,
            predicates=[
                RulePredicate(
                    name="sufficient_data_for_ml",
                    condition=lambda df, meta: len(df) >= 50,
                    description="Need at least 50 transactions for ML analysis"
                ),
                RulePredicate(
                    name="has_numeric_features",
                    condition=lambda df, meta: len(df.select_dtypes(include=[np.number]).columns) >= 2,
                    description="Need at least 2 numeric features for ML"
                ),
                RulePredicate(
                    name="not_high_risk_already",
                    condition=lambda df, meta: meta.get('risk_score', 0) < 0.8,
                    description="Skip if already high risk (early termination)"
                )
            ],
            tags=["ml", "anomaly", "expensive"],
            priority=5,
            description="Machine learning anomaly detection using Isolation Forest"
        ))

        # Inventory-specific rules
        self.register_rule(RuleMetadata(
            name="inventory_movement_anomalies",
            category=RuleCategory.INVENTORY,
            cost=RuleCost.MEDIUM,
            predicates=[
                RulePredicate(
                    name="is_inventory_file",
                    condition=lambda df, meta: meta.get('file_type') == 'inventory',
                    description="Only for inventory files"
                ),
                RulePredicate(
                    name="has_quantity_field",
                    condition=lambda df, meta: 'quantity' in df.columns,
                    description="Need quantity field for inventory analysis"
                )
            ],
            tags=["inventory", "movement", "quantity"],
            priority=6,
            description="Detect unusual inventory movement patterns"
        ))


# Global rule registry instance
rule_registry = RuleRegistry()
