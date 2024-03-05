import React from "react";
import { Routes, Route, Navigate, useLocation } from "react-router-dom";
import { Img } from "./components/Img";
import StatusPage from './pages/status';
const LoginPage = React.lazy(() => import("./pages/login"));
const ForgotPasswordPage = React.lazy(() => import('./pages/forgot-password'));
const ResetPasswordPage = React.lazy(() => import('./pages/reset-password'));
const RegisterPage = React.lazy(() => import('./pages/register'));
const ProjectRoutes = () => {
  const location = useLocation();
  const {search} = location;
  return (
    <React.Suspense fallback={<div className="w-full h-screen text-center"><Img
      src={`${process.env.PUBLIC_URL}/images/spinner-black.png`} height='20'
      className="spinner mb-px ml-2"
      alt={`Spinner`}
    /></div>}>
      {/* <Router> */}
        <Routes>
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={< ResetPasswordPage />} />
          <Route path="/status" element={<StatusPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/" element={<LoginPage search={search}/>} />
          <Route path="*" element={<Navigate to="/" />}></Route>
        </Routes>
      {/* </Router> */}
    </React.Suspense>
  );
};
export default ProjectRoutes;