import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter as Router } from 'react-router-dom';
import ForgotPasswordPage from './index';
import apiProvider from '../../services/API/IndividualApis';

jest.mock('../../services/API/IndividualApis', () => ({
  handleForgotPassword: jest.fn(),
}));

describe('ForgotPasswordPage', () => {
  test('should render the forgot password form', () => {
    const { getByPlaceholderText, getByText } = render(
      <Router>
        <ForgotPasswordPage />
      </Router>
    );

    expect(getByPlaceholderText('Email')).toBeInTheDocument();
    expect(getByText('Send Password Reset Link')).toBeInTheDocument();
  });

  test('should submit the form and call handleForgotPassword', async () => {
    const mockResponse = {
      data: {
        success: true,
        message: 'Reset Password Link sent successfully',
      },
    };

    apiProvider.handleForgotPassword.mockResolvedValueOnce(mockResponse);

    const { getByPlaceholderText, getByText } = render(
      <Router>
        <ForgotPasswordPage />
      </Router>
    );

    fireEvent.change(getByPlaceholderText('Email'), { target: { value: 'testuser@gmail.com' } });

    fireEvent.click(getByText('Send Password Reset Link'));

    await waitFor(() => {
      expect(apiProvider.handleForgotPassword).toHaveBeenCalledWith({
        email: 'testuser@gmail.com',
      });
      expect(getByText('Reset Password Link sent successfully')).toBeInTheDocument();
    });
  });

  test('should handle forgot password error', async () => {
    const mockError = {
      response: {
        data: {
          message: 'Forgot password failed',
        },
      },
    };

    apiProvider.handleForgotPassword.mockRejectedValueOnce(mockError);

    const { getByPlaceholderText, getByText } = render(
      <Router>
        <ForgotPasswordPage />
      </Router>
    );

    fireEvent.change(getByPlaceholderText('Email'), { target: { value: 'testuser@gmail.com' } });

    fireEvent.click(getByText('Send Password Reset Link'));

    await waitFor(() => {
      expect(apiProvider.handleForgotPassword).toHaveBeenCalledWith({
        email: 'testuser@gmail.com',
      });
      expect(getByText('Forgot password failed')).toBeInTheDocument();
    });
  });
});