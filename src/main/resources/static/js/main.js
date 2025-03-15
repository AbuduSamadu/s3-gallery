document.addEventListener("DOMContentLoaded", () => {
    const prevButton = document.getElementById("prev-page");
    const nextButton = document.getElementById("next-page");
    const modal = new bootstrap.Modal(document.getElementById('upload-modal'));
    const imagePreviewContainer = document.getElementById('image-preview-container');
    const imagePreview = document.getElementById('image-preview');

    // Open modal on button click
    document.getElementById('open-modal').addEventListener('click', () => {
        modal.show();
    });

    // Handle file selection for preview
    document.getElementById('image-file').addEventListener('changclear' +
        '' +
        'e', (event) => {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();

            reader.onload = (e) => {
                imagePreview.src = e.target.result; // Set the image source to the preview
                imagePreviewContainer.classList.remove('d-none'); // Show the preview container
            };

            reader.readAsDataURL(file); // Read the file as a data URL
        } else {
            imagePreview.src = '#'; // Reset the preview if no file is selected
            imagePreviewContainer.classList.add('d-none'); // Hide the preview container
        }
    });


    // Handle form submission
    document.getElementById('upload-form').addEventListener('submit', async (event) => {
        event.preventDefault();

        const formData = new FormData(event.target);
        const imageName = formData.get('imageName');
        const file = formData.get('file');

        if (!file || !imageName) {
            alert('Please provide both an image name and a file.');
            return;
        }

        try {
            const response = await fetch('/api/images/upload', {
                method: 'POST',
                body: formData,
            });

            if (response.ok) {
                alert('File uploaded successfully!');
                window.location.reload(); // Refresh the page to show the new image
            } else {
                alert('An error occurred while uploading the file.');
            }
        } catch (error) {
            console.error('Error uploading file:', error);
            alert('An error occurred while uploading the file.');
        }
    });

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


    document.addEventListener("DOMContentLoaded", async () => {
        const gallery = document.getElementById('gallery'); // Container for images

        // Fetch images from the backend
        try {
            const response = await fetch(`/api/images?page=${getPageNumber()}&size=20`);
            const data = await response.json();

            if (!data.content || !Array.isArray(data.content)) {
                console.error('Invalid API response:', data);
                return;
            }

            // Clear existing gallery content
            gallery.innerHTML = '';

            // Render images
            data.content.forEach(image => {
                const card = document.createElement('div');
                card.classList.add('card');

                const img = document.createElement('img');
                img.src = image.url; // Use the pre-signed URL
                img.alt = 'Image';
                img.classList.add('card-image');

                const info = document.createElement('div');
                info.classList.add('card-info');
                info.textContent = `Uploaded at: ${new Date(image.uploadedAt).toLocaleString()}`;

                card.appendChild(img);
                card.appendChild(info);
                gallery.appendChild(card);
            });
        } catch (error) {
            console.error('Error fetching images:', error);
        }
    });

// Helper function to get the current page number
    function getPageNumber() {
        return parseInt(new URLSearchParams(window.location.search).get("page") || 0);
    }
});