document.addEventListener("DOMContentLoaded", async () => {
    const modal = new bootstrap.Modal(document.getElementById('upload-modal'));
    const imagePreviewContainer = document.getElementById('image-preview-container');
    const imagePreview = document.getElementById('image-preview');
    const gallery = document.getElementById('gallery');
    const prevButton = document.getElementById('prev-page');
    const nextButton = document.getElementById('next-page');
    const currentPageElement = document.getElementById('current-page');
    const totalPagesElement = document.getElementById('total-pages');

    let currentPage = 0; // Start with page 0
    let totalPages = 1;  // Default to 1 page

    // Open modal on button click
    document.getElementById('open-modal').addEventListener('click', () => {
        modal.show();
    });

    // Handle file selection for preview
    document.getElementById('image-file').addEventListener('change', (event) => {
        const file = event.target.files[0];
        if (file && file.type.startsWith('image/')) { // Validate file type
            const reader = new FileReader();

            reader.onload = (e) => {
                imagePreview.src = e.target.result;
                imagePreviewContainer.classList.remove('d-none');
            };

            reader.readAsDataURL(file);
        } else {
            imagePreview.src = '#';
            imagePreviewContainer.classList.add('d-none');
            alert('Please select a valid image file.');
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

    // Fetch and render images
    const fetchAndRenderImages = async (page = 0) => {
        try {
            const response = await fetch(`/api/images/gallery?page=${page}&size=4`);
            const data = await response.json();

            if (!data.content || !Array.isArray(data.content)) {
                throw new Error('Invalid API response');
            }

            // Update pagination metadata
            currentPage = data.number || 0;
            totalPages = data.totalPages || 1;

            // Update UI elements
            currentPageElement.textContent = currentPage + 1; // Convert zero-based index to one-based
            totalPagesElement.textContent = totalPages;

            // Disable/Enable buttons based on current page
            prevButton.disabled = currentPage === 0;
            nextButton.disabled = currentPage === totalPages - 1;

            // Clear existing gallery content
            gallery.innerHTML = '';

            // Render images
            data.content.forEach(image => {
                const card = document.createElement('div');
                card.classList.add('card');

                // Image container with hover actions
                const imageContainer = document.createElement('div');
                imageContainer.classList.add('card-image-container');

                const img = document.createElement('img');
                img.src = image.url;
                img.alt = image.name;
                img.classList.add('card-image');

                // Hover actions
                const hoverActions = document.createElement('div');
                hoverActions.classList.add('card-actions-hover');

                const editButton = document.createElement('button');
                editButton.classList.add('btn', 'btn-sm', 'btn-primary', 'edit-button');
                editButton.innerHTML = '<i class="fa-solid fa-pen-to-square"></i>';
                editButton.dataset.key = image.key;
                editButton.setAttribute('aria-label', 'Edit Image');

                const deleteButton = document.createElement('button');
                deleteButton.classList.add('btn', 'btn-sm', 'btn-danger', 'delete-button');
                deleteButton.innerHTML = '<i class="fa-solid fa-trash"></i>';
                deleteButton.dataset.key = image.key;
                deleteButton.setAttribute('aria-label', 'Delete Image');

                hoverActions.appendChild(editButton);
                hoverActions.appendChild(deleteButton);

                imageContainer.appendChild(img);
                imageContainer.appendChild(hoverActions);

                // Card details
                const cardDetails = document.createElement('div');
                cardDetails.classList.add('card-details');

                const title = document.createElement('h5');
                title.classList.add('card-title');
                title.textContent = image.name;

                const timestamp = document.createElement('p');
                timestamp.classList.add('card-text');
                timestamp.textContent = `${new Date(image.uploadedAt).toLocaleString()}`;

                cardDetails.appendChild(title);
                cardDetails.appendChild(timestamp);

                // Append all parts to the card
                card.appendChild(imageContainer);
                card.appendChild(cardDetails);

                // Event listeners for edit and delete
                editButton.addEventListener('click', async () => {
                    const newName = prompt('Enter the new name for the image:', title.textContent);
                    if (newName && newName.trim() !== '') {
                        try {
                            const response = await fetch(`/api/images/${editButton.dataset.key}`, {
                                method: 'PUT',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ name: newName }),
                            });

                            if (response.ok) {
                                title.textContent = newName;
                            } else {
                                alert('Failed to update image name.');
                            }
                        } catch (error) {
                            console.error('Error updating image name:', error);
                            alert('An error occurred while updating the image name.');
                        }
                    }
                });

                deleteButton.addEventListener('click', async () => {
                    if (confirm('Are you sure you want to delete this image?')) {
                        try {
                            const response = await fetch(`/api/images/${deleteButton.dataset.key}`, {
                                method: 'DELETE',
                            });

                            if (response.ok) {
                                gallery.removeChild(card); // Remove the card from the DOM
                            } else {
                                alert('Failed to delete image.');
                            }
                        } catch (error) {
                            console.error('Error deleting image:', error);
                            alert('An error occurred while deleting the image.');
                        }
                    }
                });

                gallery.appendChild(card);
            });
        } catch (error) {
            console.error('Error fetching images:', error);
            alert('An error occurred while fetching images.');
        }
    };

    // Initial fetch
    fetchAndRenderImages(currentPage);

    // Handle Previous Page
    prevButton.addEventListener('click', () => {
        if (currentPage > 0) {
            fetchAndRenderImages(currentPage - 1);
        }
    });

    // Handle Next Page
    nextButton.addEventListener('click', () => {
        if (currentPage < totalPages - 1) {
            fetchAndRenderImages(currentPage + 1);
        }
    });
});