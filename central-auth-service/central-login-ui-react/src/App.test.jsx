import React from 'react';
import { render } from '@testing-library/react';
import '@testing-library/jest-dom/extend-expect';
import App from './App';

jest.mock('./Routes', () => () => <div data-testid="mocked-routes">Mocked Routes</div>);

test('renders App component with mocked Routes', () => {
  const { getByTestId } = render(<App />);
  expect(getByTestId('mocked-routes')).toBeInTheDocument();
});