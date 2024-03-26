import React from 'react';
import { render, fireEvent, waitFor, getByText } from '@testing-library/react';
import '@testing-library/jest-dom/extend-expect';
import LoginPage from './index';
import * as Router from 'react-router-dom';
import apiProvider from '../../services/API/IndividualApis';
const originalEnv = process.env;

const mockedNavigator = jest.fn();
// Mock window object
let originalWindowLocation = window.location;

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useParams: jest.fn(),
  useNavigate: () => mockedNavigator,
}));

beforeEach(() => {
  Object.defineProperty(window, 'location', {
    configurable: true,
    enumerable: true,
    value: new URL(window.location.href),
  });

  jest.resetModules();
  process.env = {
    ...originalEnv,
    NODE_ENV: 'YOUR_MOCKED_VALUE',
  };
});

afterEach(() => {
  Object.defineProperty(window, 'location', {
    configurable: true,
    enumerable: true,
    value: originalWindowLocation,
  });

  process.env = originalEnv;
});

jest.mock('../../services/API/IndividualApis', () => ({
  handleUserStandardLogin: jest.fn(),
  getStandardLoginStatus: jest.fn(),
  handleSamlLogin: process.env.REACT_APP_DNS + '/api/saml/login'
}));

test('renders Login Page component without crashing', () => {
  const { getByText } = render(<LoginPage />, { wrapper: Router.BrowserRouter });
  const welcomeTxt = getByText('Welcome back!');
  const ssoBtn = getByText('Login with SSO');
  const credentialLogin = getByText('Or login with credentials');
  const loginBtn = getByText('Login');
  expect(ssoBtn).toBeInTheDocument();
  expect(loginBtn).toBeInTheDocument();
  expect(welcomeTxt).toBeInTheDocument();
  expect(credentialLogin).toBeInTheDocument();
});

test('triggers SSO login when SSO button is clicked', () => {
  const { getByText } = render(<LoginPage />, { wrapper: Router.BrowserRouter });

  // Click the SSO login button
  fireEvent.click(getByText('Login with SSO'));
  // const spy = jest.spyOn(apiProvider, 'handleSamlLogin');
  // Add assertions based on the expected behavior when SSO login is triggered
  expect(window.location.href).toEqual(apiProvider.handleSamlLogin);
});

test('performs credential login and redirects with authToken', async () => {
  const mockAuthToken = 'fakeAuthToken';
  const mockRedirectUri = 'https://example.com';

  // Mock handleUserStandardLogin to resolve immediately
  apiProvider.handleUserStandardLogin.mockResolvedValueOnce({ success: true });

  // Mock getStandardLoginStatus to resolve immediately
  apiProvider.getStandardLoginStatus.mockResolvedValueOnce({
    data: {
      success: true,
      data: { authToken: mockAuthToken, email: 'test@example.com' },
    },
  });


  // Mock data for PerformCredentialLogin
  const data = {
    userName: 'testUser',
    password: 'testPassword',
  };

  // Render the LoginPage component
  const { getByPlaceholderText, getByText } = render(<LoginPage />, { wrapper: Router.BrowserRouter });

  // Simulate user input
  fireEvent.change(getByPlaceholderText('User Name'), { target: { value: 'testUser' } });
  fireEvent.change(getByPlaceholderText('Password'), { target: { value: 'testPassword' } });

  // Submit the form
  fireEvent.click(getByText('Login'));
  localStorage.setItem('redirect_uri', JSON.stringify(mockRedirectUri));
  const ls = jest.spyOn(localStorage, 'getItem').mockReturnValue(JSON.stringify(mockRedirectUri));

  // Wait for the asynchronous operations to complete
  await waitFor(() => {
    // Assert that handleUserStandardLogin was called with the correct data
    expect(apiProvider.handleUserStandardLogin).toHaveBeenCalledWith({
      username: data.userName,
      password: data.password,
    });

    // Assert that getStandardLoginStatus was called
    expect(apiProvider.getStandardLoginStatus).toHaveBeenCalled();

    // Assert that localStorage methods were called with the correct values
    // expect(localStorageMock.getItem).toHaveBeenCalledWith('redirect_uri');
    // const spy = jest.spyOn(localStorage, 'setItem');
    // expect(spy).toHaveBeenCalledWith(
    //   'user_details',
    //   JSON.stringify({ email: 'test@example.com', isAuthenticated: true })
    // );
    expect(ls).toHaveBeenCalled();

    // Assert that window.location.href was set correctly
    expect(window.location.href).toBe(`${mockRedirectUri}/?authToken=${mockAuthToken}`);
  });

  
});

test('should navigate to forgot password page', () => {
  jest.spyOn(Router, 'useNavigate').mockImplementation(() => mockedNavigator);
  const { getByText } = render(<LoginPage />, { wrapper: Router.BrowserRouter });
  fireEvent.click(getByText('Forgot Password?'));
  expect(mockedNavigator).toHaveBeenCalledTimes(1);
})