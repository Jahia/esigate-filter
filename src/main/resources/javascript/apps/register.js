(function() {
    window.jahia.i18n.loadNamespaces('esigate-filter');
    window.jahia.uiExtender.registry.add('adminRoute', 'esigate-filter', {
        targets: ['administration-server-configuration:99'],
        icon: null,
        label: 'esigate-filter:label',
        isSelectable: true,
        iframeUrl: window.contextJsParameters.contextPath + '/cms/adminframe/default/en/settings.esigate.html?redirect=false'
    });
})();
