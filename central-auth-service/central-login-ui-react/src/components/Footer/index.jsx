import React from 'react';
import './index.css';

const _currentYear = new Date().getFullYear();

const Footer = () => {
  return (
    <footer>
      <p>© {_currentYear} Publicis Sapient. All rights reserved.</p>
    </footer>
  );
};

export default Footer;