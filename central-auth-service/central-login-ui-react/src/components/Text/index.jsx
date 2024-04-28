import React from "react";

const sizeClasses = {
  txtPoppinsBold37: "font-bold font-poppins",
  txtPoppinsMedium47: "font-medium font-poppins",
  txtPoppinsMedium14: "font-medium font-poppins",
  txtPoppinsSemiBold20: "font-poppins font-semibold",
  txtPoppinsBold44: "font-bold font-poppins",
  txtPoppinsBold44WhiteA700: "font-bold font-poppins",
  txtPoppinsRegular16: "font-normal font-poppins",
  txtPoppinsMedium14Blue800: "font-medium font-poppins",
};

const Text = ({ children, className = "", size, as, ...restProps }) => {
  const Component = as || "p";

  return (
    <Component
      className={`text-left ${className} ${size && sizeClasses[size]}`}
      {...restProps}
    >
      {children}
    </Component>
  );
};

export { Text };