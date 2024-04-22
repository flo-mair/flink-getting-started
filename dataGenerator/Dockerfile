# Use a minimal Python image as the base
FROM python:3.9-slim

# Set working directory in the container
WORKDIR /app

# Copy requirements.txt to the container
COPY requirements.txt .

# Install dependencies from requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the application files to the container
COPY . .

# Command to run the Python application
CMD ["python", "main.py"]
