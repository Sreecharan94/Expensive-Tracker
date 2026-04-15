// DOM Content Loaded Event
document.addEventListener('DOMContentLoaded', () => {
    console.log('Scripts loaded successfully!');

    // Example: Add a dynamic greeting message
    const greetingElement = document.getElementById('greeting');
    if (greetingElement) {
        const currentTime = new Date();
        const hours = currentTime.getHours();
        let greetingText = '';

        if (hours < 12) {
            greetingText = 'Good Morning!';
        } else if (hours < 18) {
            greetingText = 'Good Afternoon!';
        } else {
            greetingText = 'Good Evening!';
        }

        greetingElement.textContent = greetingText;
    }

    // Example: Handle button clicks
    const buttons = document.querySelectorAll('.dynamic-button');
    buttons.forEach(button => {
        button.addEventListener('click', () => {
            alert('Button clicked!');
        });
    });
});