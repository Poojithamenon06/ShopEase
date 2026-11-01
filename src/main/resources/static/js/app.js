document.addEventListener('DOMContentLoaded', function () {
  // Mobile menu toggle
  var toggle = document.getElementById('navToggle');
  var links = document.getElementById('navLinks');
  if (toggle && links) {
    toggle.addEventListener('click', function () { links.classList.toggle('open'); });
  }

  // Auto-hide flash messages after a few seconds
  document.querySelectorAll('.flash-wrap .alert').forEach(function (el) {
    setTimeout(function () {
      el.style.transition = 'opacity .5s ease, transform .5s ease';
      el.style.opacity = '0';
      el.style.transform = 'translateY(-6px)';
      setTimeout(function () { el.remove(); }, 500);
    }, 4500);
  });

  // Filters: apply automatically when a checkbox / radio / sort option changes
  var form = document.getElementById('filterForm');
  if (form) {
    document.querySelectorAll('#filterForm input[type=checkbox], #filterForm input[type=radio], select[form=filterForm]')
      .forEach(function (el) {
        el.addEventListener('change', function () { form.submit(); });
      });
  }

  // Mobile: show / hide the filter sidebar
  var filterToggle = document.getElementById('filterToggle');
  var filters = document.getElementById('filters');
  if (filterToggle && filters) {
    filterToggle.addEventListener('click', function () { filters.classList.toggle('open'); });
  }
});
