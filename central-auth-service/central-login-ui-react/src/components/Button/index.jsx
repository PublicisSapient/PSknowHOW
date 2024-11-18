import React from "react";
import PropTypes from "prop-types";

const shapes = { round: "rounded-md" };
const variants = {
  fill: {
    blue_800: "bg-blue-800 text-white-A700",
    blue_80: "bg-blue-80 text-white-A700",
    blue_gray_100: "bg-blue_gray-100 text-blue_gray-50",
  },
};
const sizes = { xs: "p-[9px]" };

const Button = ({
  children,
  className = "",
  leftIcon,
  rightIcon,
  shape = "round",
  size = "xs",
  variant = "fill",
  color = "blue_800",
  btnType = "",
  clickFn,
  ...restProps
}) => {
  return (
    <button type={`${btnType}`} onClick={clickFn}
      className={`${className} ${(shape && shapes[shape]) || ""} ${(size && sizes[size]) || ""
        } ${(variant && variants[variant]?.[color]) || ""}`}
      {...restProps}
    >
      {!!leftIcon && leftIcon}
      {children}
      {!!rightIcon && rightIcon}
    </button>
  );
};

Button.propTypes = {
  className: PropTypes.string,
  children: PropTypes.node,
  shape: PropTypes.oneOf(["round"]),
  size: PropTypes.oneOf(["xs"]),
  variant: PropTypes.oneOf(["fill"]),
  color: PropTypes.oneOf(["blue_800", "blue_80", "blue_gray_100"]),
};

export { Button };