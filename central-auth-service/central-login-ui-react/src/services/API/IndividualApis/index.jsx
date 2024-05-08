import axios from '../Axios';

const API_BASE_URL = process.env.NODE_ENV === 'production' ? window.env.REACT_APP_DNS : process.env.REACT_APP_DNS;

const apiProvider = {
    getLoginStatus: (authToken) => {
        return axios.get(`${API_BASE_URL}/api/login/status/${authToken}`)
    },

    handleUserStandardLogin: (data) => {
        return axios.post(`${API_BASE_URL}/api/login`, data);
    },

    handleSamlLogin: `${API_BASE_URL}/api/sso-login`,

    handleUserRegistration: (data) => {
        return axios.post(`${API_BASE_URL}/api/registerUser`, data);
    },

    handleSamlLogout: `${API_BASE_URL}/api/sso-logout-view`,

    handleForgotPassword: (data) => {
        return axios.post(`${API_BASE_URL}/api/forgotPassword`, data);
    },

    handlePasswordReset: (data) => {
        return axios.post(`${API_BASE_URL}/api/resetPassword`, data);
    },

    getStandardLoginStatus: () => {
        return axios.get(`${API_BASE_URL}/api/login/status/standard`);
    }
}

export default apiProvider;
