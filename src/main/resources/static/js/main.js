document.addEventListener("DOMContentLoaded", () => {
    const prevButton = document.getElementById("prev-page");
    const nextButton = document.getElementById("next-page");

    // Handle Previous Page
    prevButton?.addEventListener("click", () => {
        const currentPage = parseInt(new URLSearchParams(window.location.search).get("page") || 0);
        window.location.search = `page=${currentPage - 1}`;
    });

    // Handle Next Page
    nextButton?.addEventListener("click", () => {
        const currentPage = parseInt(new URLSearchParams(window.location.search).get("page") || 0);
        window.location.search = `page=${currentPage + 1}`;
    });

    // Handle Upload Button
    const uploadInput = document.getElementById("image-upload");
    uploadInput?.addEventListener("change", (event) => {
        const file = event.target.files[0];
        if (file) {
            const formData = new FormData();
            formData.append("file", file);

            fetch("/api/images/upload", {
                method: "POST",
                body: formData,
            })
                .then((response) => response.json())
                .then((data) => {
                    alert("File uploaded successfully!");
                    window.location.reload();
                })
                .catch((error) => {
                    console.error("Error uploading file:", error);
                    alert("An error occurred while uploading the file.");
                });
        }
    });

    // Handle Edit and Delete Buttons
    document.querySelectorAll(".edit-button").forEach((button) => {
        button.addEventListener("click", () => {
            alert("Edit functionality not implemented yet.");
        });
    });

    document.querySelectorAll(".delete-button").forEach((button) => {
        button.addEventListener("click", () => {
            alert("Delete functionality not implemented yet.");
        });
    });
});