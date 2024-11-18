import React from 'react';
import Footer from '../Footer';

const FooterLayout = ({ children }) => {
  return (
    <div>
      <main>{children}</main>
      <Footer />
    </div>
  );
};

export default FooterLayout;