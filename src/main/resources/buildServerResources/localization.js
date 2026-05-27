(function () {
    'use strict';

    var config = window.__TC_LOCALIZATION__ || {};
    var translations = {};
    var ATTRS_TO_TRANSLATE = ['title', 'placeholder', 'alt', 'aria-label'];
    var SKIP_TAGS = { SCRIPT: 1, STYLE: 1, TEXTAREA: 1, CODE: 1, PRE: 1 };

    function translate(text) {
        if (!text) return text;
        var key = text.trim();
        if (!key) return text;
        var hit = translations[key];
        if (!hit) return text;
        // Preserve leading/trailing whitespace from original
        var leading = text.match(/^\s*/)[0];
        var trailing = text.match(/\s*$/)[0];
        return leading + hit + trailing;
    }

    function translateNode(node) {
        if (!node) return;
        if (node.nodeType === Node.TEXT_NODE) {
            var translated = translate(node.nodeValue);
            if (translated !== node.nodeValue) node.nodeValue = translated;
            return;
        }
        if (node.nodeType !== Node.ELEMENT_NODE) return;
        if (SKIP_TAGS[node.tagName]) return;

        for (var i = 0; i < ATTRS_TO_TRANSLATE.length; i++) {
            var attr = ATTRS_TO_TRANSLATE[i];
            if (node.hasAttribute && node.hasAttribute(attr)) {
                var v = node.getAttribute(attr);
                var tv = translate(v);
                if (tv !== v) node.setAttribute(attr, tv);
            }
        }

        var children = node.childNodes;
        for (var j = 0; j < children.length; j++) {
            translateNode(children[j]);
        }
    }

    function translateAll() {
        if (document.body) translateNode(document.body);
        if (document.title) {
            var t = translate(document.title);
            if (t !== document.title) document.title = t;
        }
    }

    function startObserving() {
        if (!window.MutationObserver) return;
        var observer = new MutationObserver(function (mutations) {
            for (var i = 0; i < mutations.length; i++) {
                var m = mutations[i];
                if (m.type === 'childList') {
                    for (var j = 0; j < m.addedNodes.length; j++) {
                        translateNode(m.addedNodes[j]);
                    }
                } else if (m.type === 'characterData') {
                    translateNode(m.target);
                } else if (m.type === 'attributes' && m.attributeName) {
                    if (ATTRS_TO_TRANSLATE.indexOf(m.attributeName) !== -1) {
                        translateNode(m.target);
                    }
                }
            }
        });
        observer.observe(document.body, {
            childList: true,
            subtree: true,
            characterData: true,
            attributes: true,
            attributeFilter: ATTRS_TO_TRANSLATE
        });
    }

    function init() {
        if (!config.url) {
            console.warn('[TC-i18n] no localization URL configured');
            return;
        }
        fetch(config.url, { credentials: 'same-origin' })
            .then(function (r) { return r.json(); })
            .then(function (data) {
                translations = data || {};
                translateAll();
                startObserving();
                console.info('[TC-i18n] loaded ' + Object.keys(translations).length + ' translations for ' + config.locale);
            })
            .catch(function (err) {
                console.error('[TC-i18n] failed to load translations:', err);
            });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
