import React, { useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import apiProvider from '../../services/API/IndividualApis';
import { useForm, FormProvider } from "react-hook-form";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { Img } from "../../components/Img";
import { FloatingInput } from "../../components/FloatingInput";
import '../../App.css';
import SuiteLogos from '../../components/SuiteLogos';
import PSLogo from '../../components/PSLogo';

const _loginInstanceText = process.env.NODE_ENV === 'production' ? window.env.REACT_APP_LOGIN_INSTANCE_TEXT : process.env.REACT_APP_LOGIN_INSTANCE_TEXT;

const LoginPage = ({search}) => {

    const [error, setError] = useState('');
    const [showLoader, setShowLoader] = useState(false);
    const [showSAMLLoader, setShowSAMLLoader] = useState(false);
    const [showLoginWithCredentials, setShowLoginWithCredentials] = useState(false);
    const methods = useForm({ mode: 'all' });
    const userNamePattern = /^[A-Za-z0-9]+$/;

    const PerformSAMLLogin = () => {
        setShowSAMLLoader(true);
        window.location.href = apiProvider.handleSamlLogin;
    }

    const ShowLoginWithCredentials = () => {
        setShowLoginWithCredentials(!showLoginWithCredentials);
    }

    const PerformCredentialLogin = (data) => {
        setShowLoader(true);
        apiProvider.handleUserStandardLogin({
            username: data.userName,
            password: data.password
        })
        .then((response) => {
            if(response){
                apiProvider.getStandardLoginStatus()
                .then((res) => {
                    if(res && res.data['success']){
                        // const authToken = res.data.data.authToken;
                        const redirectUri = JSON.parse(localStorage.getItem('redirect_uri'));
                        localStorage.setItem('user_details', JSON.stringify({ email: res.data.data.email, isAuthenticated: true }));
                        setShowLoader(false);
                        let defaultAppUrl = process.env.NODE_ENV === 'production' ? window.env.REACT_APP_PSKnowHOW : process.env.REACT_APP_PSKnowHOW;
                        if(!redirectUri){
                            window.location.href = (defaultAppUrl);
                        }else{
                            if(redirectUri.indexOf('?') === -1){
                                window.location.href = (`${redirectUri}`);
                            }else{
                                window.location.href = (`${redirectUri}`);
                            }
                        }
                    } else {
                     setShowLoader(false);
                     setError(res.data.message);
                     }
                }).catch((err) => {
                    console.log(err);
                    setShowLoader(false);
                    let errMessage = err?.response?.data?.message ?  err?.response?.data?.message : 'Please try again after sometime'
                    setError(errMessage);
                });
            }
        })
        .catch((err) => {
            console.log(err);
            setShowLoader(false);
            let errMessage = err?.response?.data?.message ?  err?.response?.data?.message : 'Please try again after sometime'
            setError(errMessage);
        });
    }

    useEffect(() => {
        const redirectUri = search?.split("redirect_uri=")?.[1];
        if (redirectUri) {
            localStorage.setItem('redirect_uri', JSON.stringify(redirectUri));
        }
        return () => { };
    }, [search]);

    return (
        <div className="componentContainer flex max-w-screen">
            <SuiteLogos/>
            <div className="w-2/5 p-12 bg-white-A700">
                
                <PSLogo/>
                <div className='w-full mt-8 mb-2'>
                    <Text
                        className="text-center text-lg mb-3"
                        size="txtPoppinsBold44"
                        style={{fontFamily: 'PoppinsBold'}}
                    >
                        Welcome back!
                    </Text>
                    <Text
                        className="text-center text-lg"
                    >
                        Part of Publicis Groupe?
                    </Text>
                </div>
                <Button
                    className="cursor-pointer flex min-h-[36px] items-center justify-center ml-0.5 md:ml-[0] mt-[13px] w-full"
                    rightIcon={
                        <>
                            <Img
                                className="h-5 mb-px ml-2"
                                src="images/img_arrowright.svg"
                                alt="arrow_right"
                            />
                            {showSAMLLoader && <Img
                                src={`${process.env.PUBLIC_URL}/images/spinner.png`} height='20'
                                className="spinner mb-px ml-2"
                                alt={`Spinner`}
                            />}
                        </>
                    } clickFn={PerformSAMLLogin}
                >
                    <Text className="text-white text-left">
                    Continue here
                    </Text>
                </Button>
                <Button
                    color={'white'}
                    className="cursor-pointer flex min-h-[36px] items-center justify-center ml-0.5 md:ml-[0] mt-[18px] w-full"
                    rightIcon={
                        <>
                            <Img
                                className="mb-px ml-2"
                                src="images/img_arrow_right_black.svg"
                                alt="arrow_right"
                                style={{ transform: showLoginWithCredentials ? 'rotate(90deg)' : 'translateY(2px)', }}
                            />
                        </>
                    } clickFn={ShowLoginWithCredentials}
                >
                    <Text className="text-left underline">
                       Not a part of {_loginInstanceText} ?
                    </Text>
                </Button>
              {
                showLoginWithCredentials &&
                <>
                  <FormProvider {...methods}>
                    <form noValidate autoComplete="off">
                      <FloatingInput type="text" placeHolder="Enter your user name here" id="userName"
                                     className={`mt-4 ${(methods.formState.errors['userName']) ? 'Invalid' : ''}`}
                                     validationRules={{
                                       'required': 'Field is required',
                                       'minLength': {
                                         'value': 6,
                                         'message': 'Field should contain at least 6 characters',
                                       },
                                       'pattern': {
                                         'value': userNamePattern,
                                         'message': 'Username can only contain letters and numbers',
                                       },
                                     }}>
                      </FloatingInput>
                      {(methods.formState.errors['userName']) &&
                        <p className="errMsg">{methods.formState.errors['userName'].message}</p>}
                      <FloatingInput type="password" placeHolder="Enter your password here" id="password"
                                     className={`mt-4 ${(methods.formState.errors['password']) ? 'Invalid' : ''}`}
                                     validationRules={{
                                       'required': 'Field is required',
                                       'minLength': {
                                         'value': 6,
                                         'message': 'Field should contain at least 6 characters',
                                       },
                                     }}>
                      </FloatingInput>
                      {(methods.formState.errors['password']) &&
                        <p className="errMsg">{methods.formState.errors['password'].message}</p>}

                      <Button
                        color="blue_80"
                        variant="fill"
                        className="cursor-pointer flex min-h-[36px] items-center justify-center ml-0.5 md:ml-[0] mt-[18px] w-full"
                        clickFn={methods.handleSubmit(PerformCredentialLogin)}
                        rightIcon={
                          <>
                            <Img
                              className="h-5 mb-px ml-2"
                              src="images/img_arrowright.svg"
                              alt="arrow_right"
                            />
                            {showLoader && <Img
                              src={`${process.env.PUBLIC_URL}/images/spinner.png`} height="20"
                              className="spinner mb-px ml-2"
                              alt={`Spinner`}
                            />}
                          </>
                        }
                      >
                        <Text className="text-white text-left">
                          Login
                        </Text>
                      </Button>
                      {error && error.length > 0 && <p className="errMsg">{error}</p>}
                    </form>
                  </FormProvider>
                  <div className="routeContainer mt-4">
                    <NavLink to="/forgot-password">Forgot Password?</NavLink><br/>
                    <p className="inline">Dont have an account? </p>
                    <NavLink to="/register">Sign up here.</NavLink>
                  </div>
                </>

              }
            </div>
        </div>
    );
}

export default LoginPage;
