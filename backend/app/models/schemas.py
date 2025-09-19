"""Pydantic schemas for API requests and responses."""

from pydantic import BaseModel, EmailStr, Field
from typing import Optional, List, Dict, Any
from datetime import datetime
from enum import Enum

from .user import UserRole
from .uploaded_file import FileStatus
from .alert import AlertSeverity, AlertStatus
from .audit_log import AuditAction


# User schemas
class UserBase(BaseModel):
    email: EmailStr
    username: str
    full_name: Optional[str] = None


class UserCreate(UserBase):
    password: str = Field(..., min_length=8)


class UserUpdate(BaseModel):
    email: Optional[EmailStr] = None
    username: Optional[str] = None
    full_name: Optional[str] = None
    is_active: Optional[bool] = None


class UserResponse(UserBase):
    id: int
    is_active: bool
    role: UserRole
    created_at: datetime
    updated_at: Optional[datetime] = None

    class Config:
        from_attributes = True


# Authentication schemas
class Token(BaseModel):
    access_token: str
    token_type: str


class TokenData(BaseModel):
    username: Optional[str] = None


class LoginRequest(BaseModel):
    username: str
    password: str


# File upload schemas
class UploadedFileResponse(BaseModel):
    id: int
    filename: str
    original_filename: str
    file_size: int
    content_type: str
    status: FileStatus
    total_rows: int
    processed_rows: int
    error_message: Optional[str] = None
    created_at: datetime
    updated_at: Optional[datetime] = None

    class Config:
        from_attributes = True


# Transaction schemas
class TransactionResponse(BaseModel):
    id: int
    transaction_id: Optional[str] = None
    amount: Optional[float] = None
    currency: str
    timestamp: Optional[datetime] = None
    merchant: Optional[str] = None
    location: Optional[str] = None
    payment_method: Optional[str] = None
    user_id: Optional[str] = None
    ip_address: Optional[str] = None
    device_info: Optional[str] = None
    is_suspicious: bool
    risk_score: float
    fraud_reasons: Optional[List[str]] = None
    created_at: datetime

    class Config:
        from_attributes = True


# Alert schemas
class AlertResponse(BaseModel):
    id: int
    title: str
    description: str
    severity: AlertSeverity
    status: AlertStatus
    rule_name: str
    confidence_score: float
    risk_score: float
    transaction_ids: Optional[List[int]] = None
    user_ids: Optional[List[str]] = None
    alert_metadata: Optional[Dict[str, Any]] = None
    created_at: datetime
    updated_at: Optional[datetime] = None
    resolved_at: Optional[datetime] = None

    class Config:
        from_attributes = True


class AlertUpdate(BaseModel):
    status: AlertStatus
    resolved_at: Optional[datetime] = None


# Audit log schemas
class AuditLogResponse(BaseModel):
    id: int
    action: AuditAction
    description: str
    user_id: Optional[int] = None
    ip_address: Optional[str] = None
    user_agent: Optional[str] = None
    audit_metadata: Optional[Dict[str, Any]] = None
    created_at: datetime

    class Config:
        from_attributes = True


# Dashboard schemas
class DashboardStats(BaseModel):
    total_transactions: int
    suspicious_transactions: int
    total_alerts: int
    open_alerts: int
    high_severity_alerts: int
    files_uploaded: int
    last_upload: Optional[datetime] = None


# Fraud detection schemas
class FraudAnalysisRequest(BaseModel):
    file_id: int
    rules: Optional[List[str]] = None  # Specific rules to run


class FraudAnalysisResponse(BaseModel):
    file_id: int
    total_transactions: int
    suspicious_count: int
    alerts_created: int
    analysis_time: float
    rules_applied: List[str]
    summary: Dict[str, Any]
