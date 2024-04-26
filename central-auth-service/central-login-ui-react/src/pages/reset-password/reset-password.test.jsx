import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react';
import { useNavigate } from 'react-router-dom';
import { useForm, FormProvider } from 'react-hook-form';
import ResetPasswordPage from './index';
import apiProvider from '../../services/API/IndividualApis';

jest.mock('../../services/API/IndividualApis', () => ({
  handlePasswordReset: jest.fn(),
}));

jest.mock('react-router-dom', () => ({
  useNavigate: jest.fn(),
}));

describe('ResetPasswordPage', () => {
  test('should render the reset password form', () => {
    const { getByPlaceholderText, getByText } = render(<ResetPasswordPage />);

    expect(getByPlaceholderText('Password')).toBeInTheDocument();
    expect(getByPlaceholderText('Confirm Password')).toBeInTheDocument();
    expect(getByText('Reset Password')).toBeInTheDocument();
  });

  test('should submit the form and call handlePasswordReset', async () => {
    const mockResponse = {
      data: {
        success: true,
      },
    };

    apiProvider.handlePasswordReset.mockResolvedValueOnce(mockResponse);

    const { getByPlaceholderText, getByText } = render(<ResetPasswordPage />);

    fireEvent.change(getByPlaceholderText('Password'), { target: { value: 'testPassword' } });
    fireEvent.change(getByPlaceholderText('Confirm Password'), { target: { value: 'testPassword' } });

    fireEvent.click(getByText('Reset Password'));

    await waitFor(() => {
      expect(apiProvider.handlePasswordReset).toHaveBeenCalledWith({
        password: 'testPassword',
        resetToken: null, // Replace with the actual reset token value
      });
      expect(useNavigate).toHaveBeenCalledWith('/login');
    });
  });

  test('should handle password reset error', async () => {
    const mockError = {
      response: {
        data: {
          message: 'Password reset failed',
        },
      },
    };

    apiProvider.handlePasswordReset.mockRejectedValueOnce(mockError);

    const { getByPlaceholderText, getByText } = render(<ResetPasswordPage />);

    fireEvent.change(getByPlaceholderText('Password'), { target: { value: 'testPassword' } });
    fireEvent.change(getByPlaceholderText('Confirm Password'), { target: { value: 'testPassword' } });

    fireEvent.click(getByText('Reset Password'));

    await waitFor(() => {
      expect(apiProvider.handlePasswordReset).toHaveBeenCalledWith({
        password: 'testPassword',
        resetToken: null, // Replace with the actual reset token value
      });
      expect(getByText('Password reset failed')).toBeInTheDocument();
    });
  });
});