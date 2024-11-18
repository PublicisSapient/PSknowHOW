import React from 'react';
import { Img } from "../Img";

const BgItem = ({item,className = ""}) => {
    return(
        <div className='img-container w-auto mt-4 pb-4 last-of-type:border-none'>
            <Img
                src={`${process.env.PUBLIC_URL}/images/${item}.svg`} height='90'
                className={ `h-18 mb-px ml-2 ${className}` }
                alt={`${item}`}
            />
        </div>
    )
}
export default BgItem;