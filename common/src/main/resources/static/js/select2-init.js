/**
 * Initialize Select2 for all select elements
 */
document.addEventListener('DOMContentLoaded', function () {
    // Initialize Select2 on all select elements
    $('select').each(function () {
        // Skip select elements that already have Select2 initialized
        if (!$(this).hasClass('select2-hidden-accessible')) {
            $(this).select2({
                width: '100%',
                // Add placeholder text based on the first option if it has no value
                placeholder: function () {
                    // If the first option has no value, use its text as placeholder
                    const firstOption = $(this).find('option:first-child');
                    if (firstOption.val() === '') {
                        return firstOption.text();
                    }
                    return null;
                },
                // Allow clearing the selection if the select is not required
                allowClear: !$(this).prop('required')
            });
        }
    });

    // Add custom styling to make Select2 match Bootstrap theme
    $('<style>')
        .prop('type', 'text/css')
        .html(`
            .select2-container--default .select2-selection--single {
                height: calc(1.5em + 0.75rem + 2px);
                padding: 0.375rem 0.75rem;
                font-size: 1rem;
                font-weight: 400;
                line-height: 1.5;
                border: 1px solid #ced4da;
                border-radius: 0.25rem;
            }
            .select2-container--default .select2-selection--single .select2-selection__rendered {
                line-height: 1.5;
                padding-left: 0;
                padding-right: 0;
            }
            .select2-container--default .select2-selection--single .select2-selection__arrow {
                height: calc(1.5em + 0.75rem);
            }
            .select2-container--default .select2-results__option--highlighted[aria-selected] {
                background-color: #0d6efd;
            }
            .select2-container--default.select2-container--focus .select2-selection--single {
                border-color: #86b7fe;
                box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
            }
        `)
        .appendTo('head');
});