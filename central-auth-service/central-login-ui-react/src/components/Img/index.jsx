import React from "react";

const Img = ({
  className,
  src = "defaultNoData.png",
  alt = "testImg",
  height = "",
  ...restProps
}) => {
  return (
    <img
      className={className}
      src={src}
      alt={alt}
      height= {height}
      {...restProps}
      loading={"lazy"}
    />
  );
};
export { Img };