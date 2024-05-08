import React from 'react';

const PSLogo = () => {
    // const speedSuite = process.env.NODE_ENV === 'production' ? window.env['REACT_APP_SPEED_SUITE'] : process.env.REACT_APP_SPEED_SUITE;
    // console.log("REACT_APP_SPEED_SUITE", process.env['REACT_APP_SPEED_SUITE'], window.env['REACT_APP_SPEED_SUITE'], typeof process.env['REACT_APP_SPEED_SUITE']);
    const isSpeedSuite = true;

    return(
        <>
            <div className='flex justify-center items-center'>
                <div className='h-[48px]'>
                    <img src={`${process.env.PUBLIC_URL}/images/PSLogo.svg`} alt='Publicis Sapient'/>
                </div>
                {
                    isSpeedSuite &&  
                    <div className='w-2/3 speed-suit-logo'><span>SPEED</span> <span>SUITE</span></div>
                }
            </div>
            
            <div className='w-full mt-8 accelerate-container'>Accelerate your next.</div>
        </>
    )
}

export default PSLogo;
