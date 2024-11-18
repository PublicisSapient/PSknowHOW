import axios from "axios";

const instance = axios.create({
    baseURL: process.env.NODE_ENV === 'production' ? window.env.REACT_APP_DNS : process.env.REACT_APP_DNS,
});

instance.interceptors.request.use((request) => {
    request.withCredentials = true;
    if (request.url.indexOf('login') !== -1) {
        request.headers.delete('Content-Type');
        request.headers.set('Content-Type', 'multipart/form-data');
    } else {
        request.headers.set('Content-Type', 'application/json;');
    }

    return request;
}, (error) => {
    // Handle request error
    return Promise.reject(error);
});

instance.interceptors.response.use(
    (response) => response,
    (error) => {
        // Handle response error
        console.log('Response error interceptor:', error);
        return Promise.reject(error);
    }
);
export default instance;
