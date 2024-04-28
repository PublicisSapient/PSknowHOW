import React from 'react';
import { render, waitFor } from '@testing-library/react';
import { useNavigate } from 'react-router-dom';
import apiProvider from '../../services/API/IndividualApis';
import StatusPage from './index';

jest.mock('react-router-dom', () => ({
  useNavigate: jest.fn(),
}));

jest.mock('../../services/API/IndividualApis', () => ({
  getLoginStatus: jest.fn(),
}));

describe('StatusPage', () => {
  test('should render "Login Successful" message when authToken is present', async () => {
    const mockResponse = {
      data: {
        success: true,
        data: {
          email: 'testuser@gmail.com',
        },
      },
    };

    apiProvider.getLoginStatus.mockResolvedValueOnce(mockResponse);

    const { getByText } = render(<StatusPage />);

    await waitFor(() => {
      expect(getByText('Login Successful')).toBeInTheDocument();
    });
  });

  test('should render "Authentication Failed" message when authToken is not present', async () => {
    const { getByText } = render(<StatusPage />);

    await waitFor(() => {
      expect(getByText('Authentication Failed')).toBeInTheDocument();
    });
  });

  test('should navigate to "/" when authentication fails', async () => {
    const navigateMock = jest.fn();
    useNavigate.mockReturnValue(navigateMock);

    const { getByText } = render(<StatusPage />);

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith('/');
    });
  });
});