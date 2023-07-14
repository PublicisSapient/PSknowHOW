/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

import { Component, Input, ViewContainerRef, OnChanges, SimpleChanges } from '@angular/core';
import { UntypedFormControl } from '@angular/forms';
import { UntypedFormGroup } from '@angular/forms';

@Component({
    selector: 'app-auto-complete',
    templateUrl: './auto-complete.component.html',
    styleUrls: ['./auto-complete.component.css']
})
export class AutoCompleteComponent implements OnChanges {
    @Input() data: any;
    @Input() name: string;
    @Input() controlName: UntypedFormControl;
    @Input() parentForm: UntypedFormGroup;
    elem;
    constructor(private viewContainerRef: ViewContainerRef) {
    }


    ngOnChanges(changes: SimpleChanges) {
        // only run when property "data" changed
        if (changes['data']) {
            this.elem = this.viewContainerRef.element.nativeElement;
            /*An array containing all the autoCOmplete suggestion passed*/
            const autocompleteData = this.data;
            /*initiate the autocomplete function on the "myInput" element, and pass along the countries array as possible autocomplete values:*/
            this.autocomplete(this.elem.querySelector('#autoComplete'), autocompleteData);

        }
    }


    autocomplete(inp, arr) {
        /*the autocomplete function takes two arguments,
        the text field element and an array of possible autocompleted values:*/
        let currentFocus;

        /*execute a function when someone writes in the text field:*/
        inp.addEventListener('input', function(e) {
            let a; let b; let i;
            const val = this.value;
            /*close any already open lists of autocompleted values*/

            closeAllLists(undefined);

            if (!val) {
                return false;
            }
            currentFocus = -1;
            /*create a DIV element that will contain the items (values):*/
            a = document.createElement('DIV');
            a.setAttribute('id', this.id + 'autocomplete-list');
            a.setAttribute('class', 'autocomplete-items');
            /*append the DIV element as a child of the autocomplete container:*/
            this.parentNode.appendChild(a);
            /*for each item in the array...*/
            for (i = 0; i < arr.length; i++) {
                /*check if the item starts with the same letters as the text field value:*/
                if (arr[i] && arr[i].substr(0, val.length).toUpperCase() === val.toUpperCase()) {
                    /*create a DIV element for each matching element:*/
                    b = document.createElement('DIV');
                    /*make the matching letters bold:*/
                    b.innerHTML = '<strong>' + arr[i].substr(0, val.length) + '</strong>';
                    b.innerHTML += arr[i].substr(val.length);
                    /*insert a input field that will hold the current array item's value:*/
                    b.innerHTML += '<input type=\'hidden\' value=\'' + arr[i] + '\'>';
                    /*execute a function when someone clicks on the item value (DIV element):*/
                    b.addEventListener('click', function() {
                        /*insert the value for the autocomplete text field:*/
                        inp.value = this.getElementsByTagName('input')[0].value;
                        /*close the list of autocompleted values,
                        (or any other open lists of autocompleted values:*/

                        closeAllLists(undefined);

                    });
                    a.appendChild(b);
                }
            }
        });
        /*execute a function presses a key on the keyboard:*/
        inp.addEventListener('keydown', function(e) {
            let x = document.getElementById(this.id + 'autocomplete-list');
            if (x) {
                x = (<HTMLElement><any>x.getElementsByTagName('div'));
            }
            if (e.keyCode === 40) {
                /*If the arrow DOWN key is pressed,
                increase the currentFocus variable:*/
                currentFocus++;
                /*and and make the current item more visible:*/
                addActive(x);
            } else if (e.keyCode === 38) {
                /*If the arrow UP key is pressed,
                decrease the currentFocus variable:*/
                currentFocus--;
                /*and and make the current item more visible:*/
                addActive(x);
            } else if (e.keyCode === 13) {
                /*If the ENTER key is pressed, prevent the form from being submitted,*/
                e.preventDefault();
                if (currentFocus > -1) {
                    /*and simulate a click on the "active" item:*/
                    if (x) {
                        x[currentFocus].click();
                    }
                }
            }
        });

        function addActive(x) {
            /*a function to classify an item as "active":*/
            if (!x) {
                return false;
            }
            /*start by removing the "active" class on all items:*/
            removeActive(x);
            if (currentFocus >= x.length) {
 currentFocus = 0;
}
            if (currentFocus < 0) {
 currentFocus = (x.length - 1);
}
            /*add class "autocomplete-active":*/
            x[currentFocus].classList.add('autocomplete-active');
        }

        function removeActive(x) {
            /*a function to remove the "active" class from all autocomplete items:*/
            for (let i = 0; i < x.length; i++) {
                x[i].classList.remove('autocomplete-active');
            }
        }

        function closeAllLists(elmnt) {
            /*close all autocomplete lists in the document,
            except the one passed as an argument:*/
            const x = document.getElementsByClassName('autocomplete-items');
            for (let i = 0; i < x.length; i++) {
                if (elmnt !== x[i] && elmnt !== inp) {
                    x[i].parentNode.removeChild(x[i]);
                }
            }
        }
        /*execute a function when someone clicks in the document:*/
        document.addEventListener('click', function(e) {
            closeAllLists(e.target);
        });
    }




}

