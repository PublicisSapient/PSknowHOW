import React from 'react';
import BgItem from "../../components/BgItem";

const bg = ['KnowHOWGroup', 'RetrosGroup','APGroup'];

const SuiteLogos = () => {
    const speedSuite = process.env.NODE_ENV === 'production' ? window.env['REACT_APP_SPEED_SUITE'] : process.env.REACT_APP_SPEED_SUITE;
    const isSpeedSuite = speedSuite === 'true' ? true : false;
    
    return (
        <div className="w-3/5 h-screen gradient-container">
            <div className="h-3/4 w-1/2 mt-36 m-auto">
            {
                !isSpeedSuite && 
                <BgItem key={1} item={bg[1]} />
            }
            {
                isSpeedSuite && 
                bg.map((item, index) => (
                    <BgItem key={index} item={item} />
                ))
            }
                
            </div>
            <div className="bg-image absolute h-[150px] w-[150px] bottom-0 left-0"></div>
        </div>
    )
}

export default SuiteLogos;
