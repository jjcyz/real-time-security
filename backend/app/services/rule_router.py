"""Rule router that intelligently selects rules based on predicates, risk, and cost."""

import pandas as pd
from typing import List, Dict, Any, Tuple
from dataclasses import dataclass
import time

from .rule_metadata import RuleRegistry, RuleCost, rule_registry
from .fast_signals import fast_signal_engine, FastSignal
from .schema_canonicalizer import schema_canonicalizer, FileMetadata


@dataclass
class RuleExecutionPlan:
    """Plan for executing rules with priorities and costs."""
    rules_to_execute: List[str]
    estimated_cost: float  # in milliseconds
    risk_threshold: float
    early_termination_threshold: float
    execution_order: List[str]


class RuleRouter:
    """Intelligent rule router that selects and orders rules for execution."""

    def __init__(self):
        self.rule_registry = rule_registry
        self.fast_signal_engine = fast_signal_engine
        self.schema_canonicalizer = schema_canonicalizer

        # Cost estimates in milliseconds
        self.cost_estimates = {
            RuleCost.CHEAP: 10,
            RuleCost.MEDIUM: 100,
            RuleCost.EXPENSIVE: 1000
        }

    def create_execution_plan(self, df: pd.DataFrame, file_metadata: Dict[str, Any]) -> RuleExecutionPlan:
        """Create an intelligent execution plan for rules."""

        # First, run fast signals to get initial risk assessment
        fast_signals = self.fast_signal_engine.calculate_all_signals(df, file_metadata)
        aggregate_risk = self.fast_signal_engine.get_aggregate_risk_score(fast_signals)

        # Add base risk score to ensure some rules run (minimum 0.1 for any file)
        base_risk = 0.1
        aggregate_risk = max(aggregate_risk, base_risk)

        # Update file metadata with risk score
        file_metadata['risk_score'] = aggregate_risk

        # Get applicable rules based on predicates
        applicable_rules = self.rule_registry.get_applicable_rules(df, file_metadata)

        # Filter rules based on risk and cost
        filtered_rules = self._filter_rules_by_risk_and_cost(
            applicable_rules, aggregate_risk, file_metadata
        )

        # Order rules for optimal execution
        execution_order = self._order_rules_for_execution(filtered_rules, df, file_metadata)

        # Calculate estimated cost
        estimated_cost = self._calculate_estimated_cost(execution_order)

        # Set thresholds for early termination
        early_termination_threshold = 0.9 if aggregate_risk > 0.7 else 0.8

        return RuleExecutionPlan(
            rules_to_execute=execution_order,
            estimated_cost=estimated_cost,
            risk_threshold=0.3,
            early_termination_threshold=early_termination_threshold,
            execution_order=execution_order
        )

    def _filter_rules_by_risk_and_cost(self, rules: List[str], risk_score: float,
                                     file_metadata: Dict[str, Any]) -> List[str]:
        """Filter rules based on current risk level and cost constraints."""
        filtered = []

        for rule_name in rules:
            rule_meta = self.rule_registry.get_rule_metadata(rule_name)
            if not rule_meta:
                continue

            # Skip expensive rules for low-risk files
            if (rule_meta.cost == RuleCost.EXPENSIVE and
                risk_score < 0.3 and
                file_metadata.get('file_type') != 'financial'):
                continue

            # Skip rules that don't meet risk thresholds
            if risk_score < rule_meta.risk_threshold:
                continue

            filtered.append(rule_name)

        return filtered

    def _order_rules_for_execution(self, rules: List[str], df: pd.DataFrame,
                                 file_metadata: Dict[str, Any]) -> List[str]:
        """Order rules for optimal execution (cheap first, then by priority)."""

        # Group rules by cost
        cheap_rules = []
        medium_rules = []
        expensive_rules = []

        for rule_name in rules:
            rule_meta = self.rule_registry.get_rule_metadata(rule_name)
            if not rule_meta:
                continue

            if rule_meta.cost == RuleCost.CHEAP:
                cheap_rules.append(rule_name)
            elif rule_meta.cost == RuleCost.MEDIUM:
                medium_rules.append(rule_name)
            else:
                expensive_rules.append(rule_name)

        # Sort each group by priority (higher first)
        def sort_by_priority(rule_list):
            return sorted(rule_list, key=lambda name:
                -self.rule_registry.get_rule_metadata(name).priority)

        cheap_rules = sort_by_priority(cheap_rules)
        medium_rules = sort_by_priority(medium_rules)
        expensive_rules = sort_by_priority(expensive_rules)

        # Return in execution order: cheap -> medium -> expensive
        return cheap_rules + medium_rules + expensive_rules

    def _calculate_estimated_cost(self, rules: List[str]) -> float:
        """Calculate estimated execution cost in milliseconds."""
        total_cost = 0

        for rule_name in rules:
            rule_meta = self.rule_registry.get_rule_metadata(rule_name)
            if rule_meta:
                total_cost += self.cost_estimates.get(rule_meta.cost, 100)

        return total_cost

    def should_terminate_early(self, current_risk_score: float,
                             early_termination_threshold: float) -> bool:
        """Check if we should terminate early based on current risk score."""
        return current_risk_score >= early_termination_threshold

    def get_rule_execution_stats(self, execution_plan: RuleExecutionPlan) -> Dict[str, Any]:
        """Get statistics about the execution plan."""
        rule_stats = {}

        for rule_name in execution_plan.rules_to_execute:
            rule_meta = self.rule_registry.get_rule_metadata(rule_name)
            if rule_meta:
                rule_stats[rule_name] = {
                    'category': rule_meta.category.value,
                    'cost': rule_meta.cost.value,
                    'priority': rule_meta.priority,
                    'estimated_time_ms': self.cost_estimates.get(rule_meta.cost, 100)
                }

        return {
            'total_rules': len(execution_plan.rules_to_execute),
            'estimated_total_time_ms': execution_plan.estimated_cost,
            'rule_breakdown': rule_stats,
            'early_termination_threshold': execution_plan.early_termination_threshold
        }


# Global rule router instance
rule_router = RuleRouter()
