import React, { useState, useEffect, useCallback } from 'react';
import apiProvider from '../../services/API/IndividualApis';
import { FaCircleCheck } from "react-icons/fa6";
import { FaCircleXmark } from "react-icons/fa6";
import { useNavigate } from 'react-router-dom';
import '../../App.css';

const StatusPage = () => {

    const [message, setMessage] = useState('');
    const navigate = useNavigate();

    const storeUserDetails = useCallback((authToken) => {
        const redirectUri = JSON.parse(localStorage.getItem('redirect_uri'));
        apiProvider.getLoginStatus(authToken)
            .then(function (response) {
                if (response?.data?.success) {
                    localStorage.setItem('user_details', JSON.stringify({ email: response.data.data.email, isAuthenticated: true }));
                    let defaultAppUrl = process.env.NODE_ENV === 'production' ? window.env.REACT_APP_PSKnowHOW : process.env.REACT_APP_PSKnowHOW;
                    if(!redirectUri){
                        window.location.href = defaultAppUrl;
                    }else{
                        if(redirectUri.indexOf('?') === -1){
                            window.location.href = `${redirectUri}`;
                        }else{
                            window.location.href = `${redirectUri}`;
                        }
                    }
                } else {
                    localStorage.removeItem('user_details');
                    navigate('/');
                }
            })
            .catch(function (error) {
                console.log(error);
            });
    }, [navigate]);

    useEffect(() => {
        try {
            const authToken = new URLSearchParams(window.location.search).get('authToken');
            if (authToken) {
                storeUserDetails(authToken);
                updateUIOnSuccessfulLogin();
            } else {
                handleAuthenticationFailure();
            }
        } catch (error) {
            console.log("error:", error.message);
        }
    }, [storeUserDetails]);

    const updateUIOnSuccessfulLogin = () => {
        setMessage('Login Successful');
    }

    const handleAuthenticationFailure = () => {
        setMessage('Authentication Failed');
    }

    return (
        <div className="componentContainer w-full flex items-center text-center">
            {message === 'Login Successful' &&
                <div id="status" className='w-full h-2/3 m-auto text-center text-green-500'>
                    <div className='w-full h-1/3 flex items-center text-center'><FaCircleCheck className='m-auto text-6xl' /></div>
                    <div className='w-full text-center text-2xl mt-4'>{message}</div>
                </div>
            }

            {message === 'Authentication Failed' &&
                <div id="status" className='w-full h-2/3 m-auto text-center text-red-500'>
                    <div className='w-full h-1/3 flex items-center text-center'><FaCircleXmark className='m-auto text-6xl' /></div>
                    <div className='w-full text-center text-2xl mt-4'>{message}</div>
                </div>
            }
        </div>
    );
};

export default StatusPage;
