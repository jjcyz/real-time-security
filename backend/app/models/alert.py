"""Alert model for fraud detection alerts."""

from sqlalchemy import Column, Integer, String, DateTime, Float, JSON, Enum, ForeignKey
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
import enum

from .database import Base


class AlertSeverity(str, enum.Enum):
    """Alert severity levels."""
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    CRITICAL = "critical"


class AlertStatus(str, enum.Enum):
    """Alert status."""
    OPEN = "open"
    INVESTIGATING = "investigating"
    RESOLVED = "resolved"
    FALSE_POSITIVE = "false_positive"


class Alert(Base):
    """Model for fraud detection alerts."""

    __tablename__ = "alerts"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, nullable=False)
    description = Column(String, nullable=False)
    severity = Column(Enum(AlertSeverity), default=AlertSeverity.MEDIUM)
    status = Column(Enum(AlertStatus), default=AlertStatus.OPEN)

    # Alert metadata
    rule_name = Column(String, nullable=False)  # Which fraud detection rule triggered this
    confidence_score = Column(Float, default=0.0)
    risk_score = Column(Float, default=0.0)

    # Related data
    transaction_ids = Column(JSON, nullable=True)  # Array of related transaction IDs
    user_ids = Column(JSON, nullable=True)  # Array of related user IDs

    # Additional context
    alert_metadata = Column(JSON, nullable=True)

    # User who created/resolved the alert
    created_by_user_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    resolved_by_user_id = Column(Integer, ForeignKey("users.id"), nullable=True)

    created_by_user = relationship("User", foreign_keys=[created_by_user_id])
    resolved_by_user = relationship("User", foreign_keys=[resolved_by_user_id])

    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    resolved_at = Column(DateTime(timezone=True), nullable=True)
