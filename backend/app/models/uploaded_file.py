"""Uploaded file model for tracking CSV uploads."""

from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, Enum
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
import enum

from .database import Base


class FileStatus(str, enum.Enum):
    """File processing status."""
    UPLOADED = "uploaded"
    PROCESSING = "processing"
    COMPLETED = "completed"
    FAILED = "failed"


class UploadedFile(Base):
    """Model for tracking uploaded CSV files."""

    __tablename__ = "uploaded_files"

    id = Column(Integer, primary_key=True, index=True)
    filename = Column(String, nullable=False)
    original_filename = Column(String, nullable=False)
    file_path = Column(String, nullable=False)
    file_size = Column(Integer, nullable=False)
    content_type = Column(String, nullable=False)
    status = Column(Enum(FileStatus), default=FileStatus.UPLOADED)

    # Processing metadata
    total_rows = Column(Integer, default=0)
    processed_rows = Column(Integer, default=0)
    error_message = Column(String, nullable=True)

    # User who uploaded the file
    user_id = Column(Integer, ForeignKey("users.id"))
    user = relationship("User", back_populates="uploaded_files")

    # Related transactions
    transactions = relationship("Transaction", back_populates="uploaded_file")

    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
