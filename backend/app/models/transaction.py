"""Transaction model for storing uploaded transaction data."""

from sqlalchemy import Column, Integer, String, Float, DateTime, Text, ForeignKey, JSON
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship

from .database import Base


class Transaction(Base):
    """Transaction model for storing uploaded transaction data."""

    __tablename__ = "transactions"

    id = Column(Integer, primary_key=True, index=True)
    transaction_id = Column(String, index=True, nullable=True)  # Original transaction ID from CSV
    amount = Column(Float, nullable=True)
    currency = Column(String, default="USD")
    timestamp = Column(DateTime, nullable=True)
    merchant = Column(String, nullable=True)
    location = Column(String, nullable=True)
    payment_method = Column(String, nullable=True)
    user_id = Column(String, nullable=True)  # User who made the transaction
    ip_address = Column(String, nullable=True)
    device_info = Column(String, nullable=True)

    # Additional fields stored as JSON for flexibility
    raw_data = Column(JSON, nullable=True)

    # File reference
    uploaded_file_id = Column(Integer, ForeignKey("uploaded_files.id"))
    uploaded_file = relationship("UploadedFile", back_populates="transactions")

    # Analysis results
    is_suspicious = Column(String, default=False)  # Boolean as string for flexibility
    risk_score = Column(Float, default=0.0)
    fraud_reasons = Column(JSON, nullable=True)

    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
