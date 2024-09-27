import React, {useEffect, useState} from 'react';
import {NavLink} from 'react-router-dom';
import apiProvider from '../../services/API/IndividualApis';
import {useForm, FormProvider} from "react-hook-form";
import {Text} from "../../components/Text";
import {Button} from "../../components/Button";
import {Img} from "../../components/Img";
import {FloatingInput} from "../../components/FloatingInput";
import '../../App.css';
import SuiteLogos from '../../components/SuiteLogos';
import PSLogo from '../../components/PSLogo';
import Cookies from 'js-cookie';

const SAML_USERNAME_COOKIE_NAME = "samlUsernameCookie";
const _loginButtonText = process.env.NODE_ENV === 'production' ? window.env.REACT_APP_LOGIN_BUTTON_TEXT : process.env.REACT_APP_LOGIN_BUTTON_TEXT;

const LoginPage = ({search}) => {

    const [error, setError] = useState('');
    const [showLoader, setShowLoader] = useState(false);
    const [showSAMLLoader, setShowSAMLLoader] = useState(false);
    const [showLoginWithCredentials, setShowLoginWithCredentials] = useState(false);
    const methods = useForm({mode: 'all'});
    const userNamePattern = /^[A-Za-z0-9]+$/;

    const getSamlUsernameCookie = () => {
        return Cookies.get(SAML_USERNAME_COOKIE_NAME);
    };

    const currentUsername = getSamlUsernameCookie();

    const PerformSAMLLogin = () => {
        setShowSAMLLoader(true);

        const redirectUri = JSON.parse(localStorage.getItem('redirect_uri'));

        if (redirectUri) {
            window.location.href = `${apiProvider.handleSamlLogin}?redirectUri=${redirectUri}`;
        } else {
            window.location.href = apiProvider.handleSamlLogin;
        }
    }

    const PerformSAMLLogout = async () => {
        setShowSAMLLoader(true);

        Cookies.remove(SAML_USERNAME_COOKIE_NAME, { path: '/', domain: '.tools.publicis.sapient.com' });

        window.location.href = apiProvider.handleSamlLogout
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
                if (response.status === 200) {
                    const redirectUri = JSON.parse(localStorage.getItem('redirect_uri'));

                    setShowLoader(false);

                    let url = new URL(redirectUri);

                    window.location.href = url.origin + url.pathname;
                } else {
                    setShowLoader(false);

                    setError(response.data.message);
                }
            })
            .catch((err) => {
                setShowLoader(false);

                let errMessage = err?.response?.data?.message
                    ? err?.response?.data?.message
                    : 'Please try again after sometime'

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
                <div className='w-full mt-8 mb-8'>
                    <Text
                        className="text-center text-lg"
                        size="txtPoppinsBold44"
                    >
                        Welcome back!
                    </Text>
                </div>
                <Button
                    className="cursor-pointer flex min-h-[36px] items-center justify-center ml-0.5 md:ml-[0] mt-[18px] w-full"
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
                        {currentUsername ? `Continue as ${currentUsername}` : _loginButtonText ? _loginButtonText : 'Login with SSO'}
                    </Text>
                </Button>
                {currentUsername && <Button
                    className="cursor-pointer flex min-h-[36px] items-center justify-center ml-0.5 md:ml-[0] mt-[18px] w-full"
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
                    } clickFn={PerformSAMLLogout}
                >
                    <Text className="text-white text-left">
                        Logout of Microsoft and use another account
                    </Text>
                </Button>}
                <Button
                  color={showLoginWithCredentials ? 'blue_80' : 'blue_800'}
                  className="cursor-pointer flex min-h-[36px] items-center justify-center ml-0.5 md:ml-[0] mt-[18px] w-full"
                  rightIcon={
                    <>
                      <Img
                        className="h-5 mb-px ml-2"
                        src="images/img_arrowright.svg"
                        alt="arrow_right"
                        style={{ transform: showLoginWithCredentials ? 'rotate(90deg)' : 'none' }}
                      />
                    </>
                  } clickFn={ShowLoginWithCredentials}
                >
                  <Text className="text-white text-left">
                    Login with credentials
                  </Text>
                </Button>
              {
                showLoginWithCredentials &&
                <>
                  <FormProvider {...methods}>
                    <form noValidate autoComplete="off">
                      <FloatingInput type="text" placeHolder="User Name" id="userName"
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
                      <FloatingInput type="password" placeHolder="Password" id="password"
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
                        color="blue_800"
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
