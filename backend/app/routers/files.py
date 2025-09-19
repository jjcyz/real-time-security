"""File upload and management router."""

import os
import shutil
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form
from sqlalchemy.orm import Session
from typing import List
import pandas as pd
from datetime import datetime

from ..models.database import get_db
from ..models.user import User
from ..models.uploaded_file import UploadedFile, FileStatus
from ..models.transaction import Transaction
from ..models.schemas import UploadedFileResponse
from ..utils.auth import get_current_active_user
from ..services.fraud_detection import FraudDetectionService

router = APIRouter(prefix="/files", tags=["file management"])

# Create uploads directory if it doesn't exist
UPLOAD_DIR = os.getenv("UPLOAD_DIR", "./uploads")
os.makedirs(UPLOAD_DIR, exist_ok=True)


@router.post("/upload", response_model=UploadedFileResponse)
async def upload_csv_file(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    """Upload a CSV file for fraud analysis."""

    print(f"Starting upload for file: {file.filename}")

    # Validate file type
    if not file.filename or not file.filename.endswith('.csv'):
        raise HTTPException(
            status_code=400,
            detail="Only CSV files are allowed"
        )

    # Check file size (10MB limit)
    MAX_FILE_SIZE = int(os.getenv("MAX_FILE_SIZE_MB", "10")) * 1024 * 1024
    file_content = await file.read()
    if len(file_content) > MAX_FILE_SIZE:
        raise HTTPException(
            status_code=400,
            detail=f"File size exceeds {MAX_FILE_SIZE // (1024*1024)}MB limit"
        )

    # Ensure upload directory exists
    os.makedirs(UPLOAD_DIR, exist_ok=True)

    # Generate unique filename
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"{timestamp}_{file.filename}"
    file_path = os.path.join(UPLOAD_DIR, filename)

    try:
        # Save file
        print(f"Saving file to: {file_path}")
        with open(file_path, "wb") as buffer:
            buffer.write(file_content)
        print(f"File saved successfully, size: {len(file_content)} bytes")

        # Create database record
        uploaded_file = UploadedFile(
            filename=filename,
            original_filename=file.filename,
            file_path=file_path,
            file_size=len(file_content),
            content_type=file.content_type or "text/csv",
            status=FileStatus.UPLOADED,
            user_id=current_user.id
        )

        db.add(uploaded_file)
        db.commit()
        db.refresh(uploaded_file)

        # Parse CSV and create transaction records
        try:
            parse_csv_file(uploaded_file.id, db)
        except Exception as e:
            uploaded_file.status = FileStatus.FAILED
            uploaded_file.error_message = str(e)
            db.commit()
            # Clean up the uploaded file
            if os.path.exists(file_path):
                os.remove(file_path)
            raise HTTPException(
                status_code=400,
                detail=f"Failed to parse CSV file: {str(e)}"
            )

        return uploaded_file

    except Exception as e:
        # Clean up file if database operation fails
        if os.path.exists(file_path):
            os.remove(file_path)
        raise HTTPException(
            status_code=500,
            detail=f"Upload failed: {str(e)}"
        )


def parse_csv_file(file_id: int, db: Session):
    """Parse CSV file and create transaction records."""
    uploaded_file = db.query(UploadedFile).filter(UploadedFile.id == file_id).first()
    if not uploaded_file:
        raise ValueError("File not found")

    uploaded_file.status = FileStatus.PROCESSING
    db.commit()

    try:
        # Check if file exists
        if not os.path.exists(uploaded_file.file_path):
            raise FileNotFoundError(f"Uploaded file not found at {uploaded_file.file_path}")

        # Read CSV file
        print(f"Reading CSV file: {uploaded_file.file_path}")
        df = pd.read_csv(uploaded_file.file_path)
        uploaded_file.total_rows = len(df)
        print(f"CSV file read successfully, {len(df)} rows found")

        # Create transaction records
        transactions = []
        for index, row in df.iterrows():
            try:
                # Parse timestamp
                timestamp_value = row.get('timestamp') or row.get('date') or row.get('time')
                timestamp = None
                if timestamp_value and pd.notna(timestamp_value):
                    try:
                        timestamp = pd.to_datetime(timestamp_value)
                    except Exception as e:
                        print(f"Warning: Could not parse timestamp '{timestamp_value}' on row {index}: {e}")
                        timestamp = None

                # Parse amount
                amount_value = row.get('amount') or row.get('value') or row.get('total')
                amount = None
                if amount_value and pd.notna(amount_value):
                    try:
                        amount = float(amount_value)
                    except (ValueError, TypeError):
                        print(f"Warning: Could not parse amount '{amount_value}' on row {index}")
                        amount = None

                transaction = Transaction(
                    transaction_id=str(row.get('transaction_id', row.get('id', ''))) if pd.notna(row.get('transaction_id', row.get('id'))) else None,
                    amount=amount,
                    currency=str(row.get('currency', 'USD')),
                    timestamp=timestamp,
                    merchant=str(row.get('merchant', row.get('store', row.get('vendor', '')))) if pd.notna(row.get('merchant', row.get('store', row.get('vendor')))) else None,
                    location=str(row.get('location', row.get('address', row.get('city', '')))) if pd.notna(row.get('location', row.get('address', row.get('city')))) else None,
                    payment_method=str(row.get('payment_method', row.get('method', row.get('type', '')))) if pd.notna(row.get('payment_method', row.get('method', row.get('type')))) else None,
                    user_id=str(row.get('user_id', row.get('customer_id', row.get('user', '')))) if pd.notna(row.get('user_id', row.get('customer_id', row.get('user')))) else None,
                    ip_address=str(row.get('ip_address', row.get('ip', ''))) if pd.notna(row.get('ip_address', row.get('ip'))) else None,
                    device_info=str(row.get('device_info', row.get('device', row.get('user_agent', '')))) if pd.notna(row.get('device_info', row.get('device', row.get('user_agent')))) else None,
                    raw_data=row.to_dict(),
                    uploaded_file_id=file_id
                )
                transactions.append(transaction)

            except Exception as e:
                print(f"Error processing row {index}: {e}")
                # Continue with other rows instead of failing completely
                continue

        if not transactions:
            raise ValueError("No valid transactions found in the CSV file")

        # Bulk insert transactions
        print(f"Inserting {len(transactions)} transactions into database")
        db.add_all(transactions)
        uploaded_file.status = FileStatus.COMPLETED
        uploaded_file.processed_rows = len(transactions)
        db.commit()
        print(f"Successfully processed {len(transactions)} transactions")

    except Exception as e:
        uploaded_file.status = FileStatus.FAILED
        uploaded_file.error_message = f"CSV parsing failed: {str(e)}"
        db.commit()
        raise


@router.get("/", response_model=List[UploadedFileResponse])
def get_uploaded_files(
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    """Get list of uploaded files for the current user."""
    files = db.query(UploadedFile).filter(UploadedFile.user_id == current_user.id).all()
    return files


@router.get("/{file_id}", response_model=UploadedFileResponse)
def get_uploaded_file(
    file_id: int,
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    """Get details of a specific uploaded file."""
    file = db.query(UploadedFile).filter(
        UploadedFile.id == file_id,
        UploadedFile.user_id == current_user.id
    ).first()

    if not file:
        raise HTTPException(
            status_code=404,
            detail="File not found"
        )

    return file


@router.delete("/{file_id}")
def delete_uploaded_file(
    file_id: int,
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    """Delete an uploaded file and its associated data."""
    file = db.query(UploadedFile).filter(
        UploadedFile.id == file_id,
        UploadedFile.user_id == current_user.id
    ).first()

    if not file:
        raise HTTPException(
            status_code=404,
            detail="File not found"
        )

    # Delete associated transactions
    db.query(Transaction).filter(Transaction.uploaded_file_id == file_id).delete()

    # Delete file from filesystem
    if os.path.exists(file.file_path):
        os.remove(file.file_path)

    # Delete database record
    db.delete(file)
    db.commit()

    return {"message": "File deleted successfully"}


@router.post("/{file_id}/analyze")
def analyze_file_for_fraud(
    file_id: int,
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    """Run fraud detection analysis on an uploaded file."""
    file = db.query(UploadedFile).filter(
        UploadedFile.id == file_id,
        UploadedFile.user_id == current_user.id
    ).first()

    if not file:
        raise HTTPException(
            status_code=404,
            detail="File not found"
        )

    if file.status != FileStatus.COMPLETED:
        raise HTTPException(
            status_code=400,
            detail="File must be successfully processed before analysis"
        )

    # Run fraud detection
    fraud_service = FraudDetectionService(db)
    result = fraud_service.analyze_file(file_id)

    return result
