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
import SuiteLogos from '../../components/SuiteLogos';
import PSLogo from '../../components/PSLogo';

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
            <SuiteLogos />
            <div className="w-2/5 p-12 h-screen bg-white-A700">
                <PSLogo />

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