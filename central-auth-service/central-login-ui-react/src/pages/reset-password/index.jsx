import React, { useState } from 'react';
import { useForm, FormProvider } from "react-hook-form";
import { NavLink } from 'react-router-dom';
import apiProvider from '../../services/API/IndividualApis';
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { Img } from "../../components/Img";
import { FloatingInput } from "../../components/FloatingInput";
import { useNavigate } from 'react-router-dom';
import '../../App.css';
import BgItem from '../../components/BgItem';

const bg = ['KnowHOWGroup', 'RetrosGroup', 'APGroup'];

const ResetPasswordPage = () => {

    const methods = useForm({ mode: 'all' });

    const navigate = useNavigate();

    const [error, setError] = useState(null);
    
    const passwordPattern = /(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[$@$!%*?&])[A-Za-z\d$@$!%*?&].{7,}/;
    const passwordError = 'At least 8 characters in length with Lowercase letters, Uppercase letters, Numbers and Special characters';
    const password = methods.watch('password', '');

    const onSubmit = (data) => {
        const resetToken = new URLSearchParams(window.location.search).get('resetToken');
        const obj = { password: data['password'], resetToken: resetToken };

        apiProvider.handlePasswordReset(obj)
            .then(function (response) {
                if (response?.data?.success) {
                    navigate('/login');
                }
            })
            .catch(function (error) {
                console.log(error.response.data.message);
                setError(error.response.data.message);
            });
    }

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

                <Text className='text-left mt-4' size='txtPoppinsSemiBold20'>Reset password</Text>
                <FormProvider {...methods}>
                    <form onSubmit={e => e.preventDefault()} noValidate autoComplete='off'>
                        <FloatingInput type="password" placeHolder="Password" id="password" className={`mt-4 ${(methods.formState.errors['password']) ? 'Invalid' : ''}`}
                            validationRules={{
                                "required": "Field is required",
                                "pattern": {
                                    "value": passwordPattern,
                                    "message": passwordError
                                }
                            }}>
                        </FloatingInput>
                        {(methods.formState.errors['password']) && <p className='errMsg'>{methods.formState.errors['password'].message}</p>}
                        <FloatingInput type="password" placeHolder="Confirm Password" id="confirmPassword" className={`mt-4 ${(methods.formState.errors['confirmPassword']) ? 'Invalid' : ''}`}
                            validationRules={{
                                "required": "Field is required",
                            }}
                            validateValueFn={(value) => {
                                return value === password || 'Passwords must match'
                            }}
                        >
                        </FloatingInput>
                        {(methods.formState.errors['confirmPassword']) && <p className='errMsg'>{methods.formState.errors['confirmPassword'].message}</p>}
                        {error && <p className="errMsg">{error}</p>}
                        <Button
                            className="cursor-pointer flex min-h-[36px] items-center justify-center ml-0.5 md:ml-[0] mt-[18px] w-full"
                            clickFn={methods.handleSubmit(onSubmit)}
                            rightIcon={
                                <Img
                                    className="h-5 mb-px ml-2"
                                    src="images/img_arrowright.svg"
                                    alt="arrow_right"
                                />
                            }
                        >
                            <Text className="text-white text-left">
                                Reset Password
                            </Text>
                        </Button>
                    </form>
                </FormProvider>
                <div className="routeContainer mt-4">
                    <NavLink to="/">Login here.</NavLink>
                </div>
            </div>
        </div>
    );
}

export default ResetPasswordPage;