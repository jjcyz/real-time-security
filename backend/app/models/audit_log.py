"""Audit log model for tracking user actions."""

from sqlalchemy import Column, Integer, String, DateTime, JSON, ForeignKey, Enum
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
import enum

from .database import Base


class AuditAction(str, enum.Enum):
    """Types of audit actions."""
    LOGIN = "login"
    LOGOUT = "logout"
    FILE_UPLOAD = "file_upload"
    FILE_DELETE = "file_delete"
    ALERT_VIEW = "alert_view"
    ALERT_RESOLVE = "alert_resolve"
    USER_CREATE = "user_create"
    USER_UPDATE = "user_update"
    USER_DELETE = "user_delete"
    FRAUD_ANALYSIS = "fraud_analysis"


class AuditLog(Base):
    """Model for audit logging."""

    __tablename__ = "audit_logs"

    id = Column(Integer, primary_key=True, index=True)
    action = Column(Enum(AuditAction), nullable=False)
    description = Column(String, nullable=False)

    # User who performed the action
    user_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    user = relationship("User", back_populates="audit_logs")

    # Request metadata
    ip_address = Column(String, nullable=True)
    user_agent = Column(String, nullable=True)

    # Additional context
    audit_metadata = Column(JSON, nullable=True)

    created_at = Column(DateTime(timezone=True), server_default=func.now())
