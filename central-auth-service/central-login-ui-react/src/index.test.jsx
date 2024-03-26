import React from 'react';
import ReactDOM from 'react-dom';
import { act } from '@testing-library/react';

import App from './App';

jest.mock('react-dom', () => ({
  createRoot: jest.fn().mockReturnValue({
    render: jest.fn(),
  }),
}));

describe('index', () => {
  test('should render the App component', () => {
    act(() => {
      require('./index');
    });

    expect(ReactDOM.createRoot).toHaveBeenCalledWith(document.getElementById('root'));
    expect(ReactDOM.createRoot().render).toHaveBeenCalledWith(<App />);
  });
});