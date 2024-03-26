import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react';
import RegisterPage from './index';
import apiProvider from '../../services/API/IndividualApis';
import { useNavigate } from 'react-router-dom';

jest.mock('../../services/API/IndividualApis', () => ({
  handleUserRegistration: jest.fn(),
}));

jest.mock('react-router-dom', () => ({
  useNavigate: jest.fn(),
}));

describe('RegisterPage', () => {
  test('should render the registration form', () => {
    const { getByPlaceholderText, getByText } = render(<RegisterPage />);

    expect(getByPlaceholderText('User Name')).toBeInTheDocument();
    expect(getByPlaceholderText('Password')).toBeInTheDocument();
    expect(getByPlaceholderText('Confirm Password')).toBeInTheDocument();
    expect(getByPlaceholderText('Email')).toBeInTheDocument();
    expect(getByPlaceholderText('Display Name')).toBeInTheDocument();
    expect(getByText('Register')).toBeInTheDocument();
  });

  test('should submit the form and call handleUserRegistration', async () => {
    const mockResponse = {
      data: {
        success: true,
        message: 'User Verification Request mail sent successfully.',
      },
    };

    apiProvider.handleUserRegistration.mockResolvedValueOnce(mockResponse);

    const { getByPlaceholderText, getByText } = render(<RegisterPage />);

    fireEvent.change(getByPlaceholderText('User Name'), { target: { value: 'testUser' } });
    fireEvent.change(getByPlaceholderText('Password'), { target: { value: 'testPassword' } });
    fireEvent.change(getByPlaceholderText('Confirm Password'), { target: { value: 'testPassword' } });
    fireEvent.change(getByPlaceholderText('Email'), { target: { value: 'testuser@gmail.com' } });
    fireEvent.change(getByPlaceholderText('Display Name'), { target: { value: 'Test User' } });

    fireEvent.click(getByText('Register'));

    await waitFor(() => {
      expect(apiProvider.handleUserRegistration).toHaveBeenCalledWith({
        username: 'testUser',
        password: 'testPassword',
        email: 'testuser@gmail.com',
        displayName: 'Test User',
      });
      expect(getByText('User Verification Request mail sent successfully.')).toBeInTheDocument();
    });
  });

  test('should handle registration error', async () => {
    const mockError = {
      response: {
        data: {
          message: 'Registration failed',
        },
      },
    };

    apiProvider.handleUserRegistration.mockRejectedValueOnce(mockError);

    const { getByPlaceholderText, getByText } = render(<RegisterPage />);

    fireEvent.change(getByPlaceholderText('User Name'), { target: { value: 'testUser' } });
    fireEvent.change(getByPlaceholderText('Password'), { target: { value: 'testPassword' } });
    fireEvent.change(getByPlaceholderText('Confirm Password'), { target: { value: 'testPassword' } });
    fireEvent.change(getByPlaceholderText('Email'), { target: { value: 'testuser@gmail.com' } });
    fireEvent.change(getByPlaceholderText('Display Name'), { target: { value: 'Test User' } });

    fireEvent.click(getByText('Register'));

    await waitFor(() => {
      expect(apiProvider.handleUserRegistration).toHaveBeenCalledWith({
        username: 'testUser',
        password: 'testPassword',
        email: 'testuser@gmail.com',
        displayName: 'Test User',
      });
      expect(getByText('Registration failed')).toBeInTheDocument();
    });
  });
});