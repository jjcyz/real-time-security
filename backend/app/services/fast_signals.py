"""Fast signals and cheap risk models for early filtering and risk assessment."""

import pandas as pd
import numpy as np
from typing import Dict, Any, Tuple, List
from dataclasses import dataclass
from datetime import datetime, timedelta


@dataclass
class FastSignal:
    """Result from a fast signal calculation."""
    name: str
    value: float
    risk_score: float
    confidence: float
    metadata: Dict[str, Any]


class FastSignalEngine:
    """Engine for calculating fast, cheap risk signals."""

    def __init__(self):
        self.signals = {}
        self._register_default_signals()

    def calculate_all_signals(self, df: pd.DataFrame, file_metadata: Dict[str, Any]) -> List[FastSignal]:
        """Calculate all applicable fast signals."""
        results = []

        for signal_name, signal_func in self.signals.items():
            try:
                signal_result = signal_func(df, file_metadata)
                if signal_result:
                    results.append(signal_result)
            except Exception as e:
                # Skip signals that fail rather than crashing
                print(f"Fast signal {signal_name} failed: {e}")
                continue

        return results

    def get_aggregate_risk_score(self, signals: List[FastSignal]) -> float:
        """Calculate aggregate risk score from fast signals."""
        if not signals:
            return 0.0

        # Weight signals by confidence and combine
        weighted_scores = []
        total_weight = 0

        for signal in signals:
            weight = signal.confidence
            weighted_scores.append(signal.risk_score * weight)
            total_weight += weight

        if total_weight == 0:
            return 0.0

        return sum(weighted_scores) / total_weight

    def _register_default_signals(self):
        """Register default fast signals."""

        def data_quality_signal(df: pd.DataFrame, meta: Dict[str, Any]) -> FastSignal:
            """Signal based on data quality issues."""
            if len(df) == 0:
                return None

            # Calculate missing data ratio
            missing_ratio = df.isnull().sum().sum() / (len(df) * len(df.columns))

            # Calculate risk score (0-1)
            risk_score = min(missing_ratio * 2, 1.0)  # Scale up missing data impact

            return FastSignal(
                name="data_quality",
                value=float(missing_ratio),
                risk_score=float(risk_score),
                confidence=0.8,
                metadata={"missing_ratio": float(missing_ratio), "total_cells": int(len(df) * len(df.columns))}
            )

        def file_size_signal(df: pd.DataFrame, meta: Dict[str, Any]) -> FastSignal:
            """Signal based on file size (very large files might be suspicious)."""
            row_count = len(df)

            # Risk increases with file size (potential data dump)
            if row_count > 10000:
                risk_score = 0.3
            elif row_count > 5000:
                risk_score = 0.1
            else:
                risk_score = 0.0

            return FastSignal(
                name="file_size",
                value=int(row_count),
                risk_score=float(risk_score),
                confidence=0.6,
                metadata={"row_count": int(row_count)}
            )

        def amount_distribution_signal(df: pd.DataFrame, meta: Dict[str, Any]) -> FastSignal:
            """Signal based on amount distribution patterns."""
            if 'amount' not in df.columns or df['amount'].isnull().all():
                return None

            amounts = df['amount'].dropna()
            if len(amounts) < 5:
                return None

            # Check for suspicious patterns
            risk_score = 0.0

            # All amounts are identical (potential data issue)
            if amounts.nunique() == 1:
                risk_score += 0.4

            # Very high variance (potential outliers)
            cv = amounts.std() / amounts.mean() if amounts.mean() != 0 else 0
            if cv > 3:  # Coefficient of variation > 3
                risk_score += 0.3

            # Round number bias (suspicious in financial data)
            round_numbers = (amounts % 1 == 0).sum() / len(amounts)
            if round_numbers > 0.8:
                risk_score += 0.2

            return FastSignal(
                name="amount_distribution",
                value=float(cv),
                risk_score=float(min(risk_score, 1.0)),
                confidence=0.7,
                metadata={
                    "coefficient_of_variation": float(cv),
                    "round_number_ratio": float(round_numbers),
                    "unique_values": int(amounts.nunique())
                }
            )

        def temporal_pattern_signal(df: pd.DataFrame, meta: Dict[str, Any]) -> FastSignal:
            """Signal based on temporal patterns."""
            if 'timestamp' not in df.columns or df['timestamp'].isnull().all():
                return None

            try:
                timestamps = pd.to_datetime(df['timestamp']).dropna()
                if len(timestamps) < 5:
                    return None

                risk_score = 0.0

                # Check for time clustering (all transactions at same time)
                time_diff = timestamps.diff().dropna()
                if len(time_diff) > 0 and (time_diff < timedelta(minutes=1)).sum() > len(time_diff) * 0.8:
                    risk_score += 0.4  # 80%+ transactions within 1 minute

                # Check for future timestamps
                now = datetime.now()
                future_timestamps = (timestamps > now).sum()
                if future_timestamps > 0:
                    risk_score += 0.3

                # Check for very old timestamps (potential data quality issue)
                old_timestamps = (timestamps < now - timedelta(days=365)).sum()
                if old_timestamps > len(timestamps) * 0.5:
                    risk_score += 0.2

                return FastSignal(
                    name="temporal_pattern",
                    value=int(len(time_diff)),
                    risk_score=float(min(risk_score, 1.0)),
                    confidence=0.6,
                    metadata={
                        "future_timestamps": int(future_timestamps),
                        "old_timestamps": int(old_timestamps),
                        "time_clustering": int((time_diff < timedelta(minutes=1)).sum()) if len(time_diff) > 0 else 0
                    }
                )
            except:
                return None

        def user_behavior_signal(df: pd.DataFrame, meta: Dict[str, Any]) -> FastSignal:
            """Signal based on user behavior patterns."""
            if 'user_id' not in df.columns or df['user_id'].isnull().all():
                return None

            user_counts = df['user_id'].value_counts()
            if len(user_counts) < 2:
                return None

            risk_score = 0.0

            # Single user dominates (potential account takeover)
            max_user_ratio = user_counts.max() / len(df)
            if max_user_ratio > 0.8:
                risk_score += 0.4

            # Too many unique users for file size (potential data issue)
            user_diversity = len(user_counts) / len(df)
            if user_diversity > 0.9 and len(df) > 100:
                risk_score += 0.2

            return FastSignal(
                name="user_behavior",
                value=float(user_diversity),
                risk_score=float(min(risk_score, 1.0)),
                confidence=0.7,
                metadata={
                    "max_user_ratio": float(max_user_ratio),
                    "user_diversity": float(user_diversity),
                    "unique_users": int(len(user_counts))
                }
            )

        # Register all signals
        self.signals = {
            "data_quality": data_quality_signal,
            "file_size": file_size_signal,
            "amount_distribution": amount_distribution_signal,
            "temporal_pattern": temporal_pattern_signal,
            "user_behavior": user_behavior_signal
        }


# Global fast signal engine
fast_signal_engine = FastSignalEngine()
