import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useForm, FormProvider } from "react-hook-form";
import apiProvider from '../../services/API/IndividualApis';
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { Img } from "../../components/Img";
import { FloatingInput } from "../../components/FloatingInput";
import { useNavigate } from 'react-router-dom';
import '../../App.css';
import SuiteLogos from '../../components/SuiteLogos';
import PSLogo from '../../components/PSLogo';

const RegisterPage = () => {

    const methods = useForm({ mode: 'all' });
    const [showLoader, setShowLoader] = useState(false);
    const [success, setSuccess] = useState(null);
    const navigate = useNavigate();

    const [error, setError] = useState(null);
    const passwordPattern = /(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[$@$!%*?&])[A-Za-z\d$@$!%*?&].{7,}/;
    const passwordError = 'At least 8 characters in length with Lowercase letters, Uppercase letters, Numbers and Special characters';
    const emailPatthern = /[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$/;
    const userNamePattern = /^[A-Za-z0-9]+$/;
    const password = methods.watch('password', '');

    const restrictedDomains = [
        "publicisgroupe.net",
        "publicissapient.com",
        "publicisresources.com"
    ];

    const onSubmit = (data) => {
        setShowLoader(true);
        let obj = {
            username: data.userName,
            password: data.password,
            email: data.email,
            displayName: data.displayName
        }
        obj['firstName'] = obj['displayName']?.split(" ")[0];
        obj['lastName'] = obj['displayName']?.split(" ")[1] || '';
        apiProvider.handleUserRegistration(obj)
            .then(function (response) {
                if (response?.data?.success) {
                    setShowLoader(false);
                    if(response?.data?.message){
                        setSuccess(response?.data?.message);
                        setError('');
                    }
                    setTimeout(() => {
                        navigate('/');
                    }, 5000);
                }
            })
            .catch(function (error) {
                console.log(error);
                let errMessage = error?.response?.data?.message ?  error?.response?.data?.message : 'Please try again after sometime'
                setError(errMessage);
                setSuccess('');
                setShowLoader(false);
            });
    }

    return (
        <div className="componentContainer flex w-full">
            <SuiteLogos />
            <div className="w-2/5 p-12 bg-white-A700">
                <PSLogo />

                <Text className='text-left mt-4' size='txtPoppinsSemiBold20'>Create an account</Text>
                <FormProvider {...methods}>
                    <form onSubmit={e => e.preventDefault()} noValidate autoComplete='off'>
                        <FloatingInput type="text" placeHolder="User Name" id="userName" className={`mt-4 ${(methods.formState.errors['userName']) ? 'Invalid' : ''}`}
                            validationRules={{
                                "required": "Field is required",
                                "minLength": {
                                    "value": 6,
                                    "message": "Field should contain at least 6 characters"
                                },
                                "pattern": {
                                    "value": userNamePattern,
                                    "message": 'Username can only contain letters and numbers'
                                }
                            }}>
                        </FloatingInput>
                        {(methods.formState.errors['userName']) && <p className='errMsg'>{methods.formState.errors['userName'].message}</p>}
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
                        <FloatingInput type="text" placeHolder="Email" id="email" className={`mt-4 ${(methods.formState.errors['password']) ? 'Invalid' : ''}`}
                            validationRules={{
                                "required": "Field is required",
                                "pattern": {
                                    "value": emailPatthern,
                                    "message": 'Invalid Email'
                                },
                            }}
                            validateValueFn={(value) => {
                                const domain = value.split('@')[1].toLowerCase(); // Extract the domain and convert to lowercase
                                if (restrictedDomains.includes(domain)) {
                                    return `The email domain ${domain} is not allowed.Please use a non-groupe email domain.`;
                                }
                                return true; // Return true if the domain is not restricted
                            }}>
                        </FloatingInput>
                        {(methods.formState.errors['email']) && <p className='errMsg'>{methods.formState.errors['email'].message}</p>}
                        <FloatingInput type="text" placeHolder="Display Name" id="displayName" className={`mt-4 ${(methods.formState.errors['displayName']) ? 'Invalid' : ''}`}
                            validationRules={{
                                "required": "Field is required",
                                "minLength": {
                                    "value": 6,
                                    "message": "Field should contain at least 6 characters"
                                }
                            }}>
                        </FloatingInput>
                        {(methods.formState.errors['displayName']) && <p className='errMsg'>{methods.formState.errors['displayName'].message}</p>}
                        {/* {error && error.length > 0 && <p className='errMsg'>{error}</p>} */}
                        <Button
                            className="cursor-pointer flex min-h-[36px] items-center justify-center ml-0.5 md:ml-[0] mt-[18px] w-full"
                            clickFn={methods.handleSubmit(onSubmit)}
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
                                Register
                            </Text>
                        </Button>
                        {success && <p className='successMsg'>{success}</p>}
                        {error && <p className='errMsg'>{error}</p>}
                    </form>
                </FormProvider>
                <div className="routeContainer mt-4">
                    <p className='inline'>Already have an account? </p>
                    <NavLink to="/">Login here.</NavLink>
                </div>
            </div>
        </div>
    );
}

export default RegisterPage;