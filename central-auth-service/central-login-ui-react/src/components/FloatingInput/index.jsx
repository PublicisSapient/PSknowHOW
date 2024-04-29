import React from "react";
import { useFormContext } from 'react-hook-form';
import './index.css';

function FloatingInput({ className, placeHolder, id, type, onChangeFn = () => { }, validationRules = {}, validateValueFn = () => { } }) {
    // const [isActive, setIsActive] = useState(false);

    const { register } = useFormContext();

    function handleTextChange(e) {
        // if (e.target.value !== '') {
        //     setIsActive(true);
        // } else {
        //     setIsActive(false);
        // }
        onChangeFn(e);
    }

    return (
        <div className={`float-label ${className}`}>
            <input id={id} type={type} placeholder={placeHolder}
                {...register(`${id}`, {
                    ...validationRules,
                    onChange: handleTextChange,
                    validate: validateValueFn // Provide the onChange handler
                })}
            />
            {/* <label className={`${activeClass}`} htmlFor={id} >
                {placeHolder}
            </label> */}
        </div>
    );
};

export { FloatingInput };