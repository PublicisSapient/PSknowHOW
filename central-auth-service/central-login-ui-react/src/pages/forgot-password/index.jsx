import React, { useState, useEffect } from 'react';
import { NavLink } from 'react-router-dom';
import apiProvider from '../../services/API/IndividualApis';
import { useForm, FormProvider } from "react-hook-form";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { Img } from "../../components/Img";
import { FloatingInput } from "../../components/FloatingInput";
import '../../App.css';
import BgItem from '../../components/BgItem';

const bg = ['KnowHOWGroup', 'RetrosGroup', 'APGroup'];

const ForgotPasswordPage = () => {

    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [showLoader, setShowLoader] = useState(false);
    const methods = useForm({ mode: 'all' });
    const emailPatthern = /[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$/;
    const SendPasswordLink = (data) => {
        setShowLoader(true);
        apiProvider.handleForgotPassword({
            email: data.email
        })
            .then(function (response) {
                console.log(response);
                setSuccess('Reset Password Link sent successfully');
                setError('');
                methods.formState.errors['email'] = '';
                setShowLoader(false);
            })
            .catch(function (error) {
                console.log(error.response.data.message);
                setError(error.response.data.message);
                setSuccess('');
                methods.formState.errors['email'] = '';
                setShowLoader(false);
            });
    }

    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        const redirectUri = urlParams.get('redirect_uri');
        if (redirectUri) {
            localStorage.setItem('redirect_uri', JSON.stringify(redirectUri));
        }
        return () => {
        };
    }, []);

    return (
        <div className="componentContainer flex h-screen w-full">
            <div className="w-3/5 h-screen gradient-container">
                <div className="h-3/4 w-1/2 mt-36 m-auto">
                    {bg.map((item, index) => (
                        <BgItem key={index} item={item} />
                    ))}
                </div>
                <div className="bg-image absolute h-[150px] w-[150px] bottom-0 left-0"></div>
            </div>
            <div className="w-2/5 p-12 h-screen bg-white-A700">
                <div className='flex items-center'>
                    <div className='h-[48px] w-1/3 ps-logo'></div>
                    <div className='w-2/3 speed-suit-logo'><span>SPEED</span> <span>SUIT</span></div>
                </div>
                <div className='w-full mt-8 accelerate-container'>Accelerate your next.</div>

                <Text className='text-left mt-4' size='txtPoppinsSemiBold20'>Enter your Email</Text>
                <FormProvider {...methods}>
                    <form onSubmit={e => e.preventDefault()} noValidate autoComplete='off'>
                        <FloatingInput type="text" placeHolder="Email" id="email" className={`mt-4 ${(methods.formState.errors['email']) ? 'Invalid' : ''}`}
                            validationRules={{
                                "required": "Field is required",
                                "pattern": {
                                    "value": emailPatthern,
                                    "message": 'Invalid Email'
                                }
                            }}>
                        </FloatingInput>
                        {(methods.formState.errors['email']) && <p className='errMsg'>{methods.formState.errors['email'].message}</p>}
                        {error && error.length && !methods.formState.errors['email']> 0 && <p className='errMsg'>{error}</p>}
                        {success && success.length && !methods.formState.errors['email']> 0 && <p className='successMsg'>{success}</p>}
                        <Button
                            className="cursor-pointer flex min-h-[36px] items-center justify-center ml-0.5 md:ml-[0] mt-[18px] w-full"
                            clickFn={methods.handleSubmit(SendPasswordLink)}
                            rightIcon={
                                <>
                                    <Img
                                        className="h-5 mb-px ml-2"
                                        src="images/img_arrowright.svg"
                                        alt="arrow_right"
                                    />
                                    {showLoader && <Img
                                        src={`${process.env.PUBLIC_URL}/images/spinner.png`} height='20'
                                        className="spinner mb-px ml-2"
                                        alt={`Spinner`}
                                    />}
                                </>
                            }
                        >
                            <Text className="text-white text-left">
                                Send Password Reset Link
                            </Text>
                        </Button>
                    </form>
                </FormProvider>
                <div className="routeContainer mt-4">
                    <p className='inline'>Dont have an account? </p>
                    <NavLink to="/register">Sign up here.</NavLink>
                </div>
            </div>
        </div>
    );
}

export default ForgotPasswordPage;