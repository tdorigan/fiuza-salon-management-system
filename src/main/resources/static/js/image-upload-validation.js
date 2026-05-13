
//wait until the full html page has loaded before searching for elements
document.addEventListener("DOMContentLoaded", function () {

    //find every file input that should have image size validation
    document.querySelectorAll(".image-upload-input").forEach(function (input) {

        //get the direct parent element of the input
        //this assumes the error div is placed in the same container as the input
        const parent = input.parentElement;

        //find the error div inside the same parent container
        //this avoids using ids and keeps the code reusable for multiple inputs
        const errorDiv = parent.querySelector(".image-upload-error");

        //add an event that runs whenever the user selects a file
        input.addEventListener("change", function () {

            if (errorDiv) {

                //hide the error message every time the user changes the selected file
                errorDiv.style.display = "none";

                //clear any previous error text inside the div
                errorDiv.textContent = "";

            }

            //get the first selected file from the input
            const file = input.files[0];

            //read the maximum allowed size from the data-max-size attribute
            const maxSizeBytes = Number(input.dataset.maxSize);

            //read the translated error message from the data-error-message attribute
            const errorMessage = input.dataset.errorMessage;

            //if no file was selected, stop the function
            if (!file) {
                return;
            }

            //if the selected file is bigger than the configured max size
            if (file.size > maxSizeBytes) {

                if (errorDiv) {

                    //set the translated error message inside the error div
                    errorDiv.textContent = errorMessage;

                    //display the error div so the user can see the message
                    errorDiv.style.display = "block";

                }

                //clear the selected file so the invalid file is not submitted
                input.value = "";
            }
        });
    });
});
