<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title><g:layoutTitle default="ZelifKudos"/></title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico"/>
    <link rel="manifest" href="/manifest.json"/>
    <link rel="apple-touch-icon" href="/icons/apple-touch-icon.png"/>
    <meta name="theme-color" content="#c0c0c0"/>
    <meta name="apple-mobile-web-app-capable" content="yes"/>
    <meta name="apple-mobile-web-app-status-bar-style" content="default"/>
    <meta name="apple-mobile-web-app-title" content="ZelifKudos"/>
    <asset:stylesheet src="application.css"/>
    <g:layoutHead/>
</head>

<body>

<div class="win-desktop-layout">

    <g:if test="${session.userId}">
    <div class="win-window win-chat-window" id="chatWindow">
        <div class="win-titlebar" id="chatTitlebar" style="cursor:grab;">
            <span class="win-titlebar-text">Random Chat</span>
            <button class="win-titlebar-btn" onclick="toggleChat()" title="Minimize">&minus;</button>
        </div>
        <div class="win-chat-body" id="chatBody">
            <div class="win-sunken win-chat-messages" id="chatMessages"></div>
            <div class="win-chat-input-area">
                <input type="text" id="chatInput" placeholder="Type a message..." maxlength="500"
                       onkeydown="if(event.key==='Enter'&&!event.isComposing)sendChatMessage()" />
                <button class="win-btn win-btn-sm" onclick="sendChatMessage()">Send</button>
            </div>
        </div>
    </div>
    </g:if>

    <div class="win-window">
        <div class="win-titlebar">
            <span class="win-titlebar-text">ZelifKudos - Employee Recognition System</span>
        </div>
        <div class="win-menubar">
            <a href="${createLink(controller:'user', action:'list')}" class="${controllerName == 'user' ? 'active' : ''}">Users</a>
            <a href="${createLink(controller:'kudos', action:'list')}" class="${controllerName == 'kudos' && actionName == 'list' ? 'active' : ''}">History</a>
            <a href="${createLink(controller:'kudos', action:'myKudos')}" class="${controllerName == 'kudos' && actionName == 'myKudos' ? 'active' : ''}">My Kudos</a>
        </div>
        <div class="win-body">
            <g:layoutBody/>
        </div>
        <div class="win-statusbar">
            <span class="win-statusbar-panel">Ready</span>
            <span class="win-statusbar-panel" style="flex:0; white-space:nowrap;">ZelifKudos v<g:meta name="info.app.version"/></span>
        </div>
    </div>

</div>

<div class="win-taskbar">
    <a href="${request.contextPath}/" class="win-start-btn">
        <span class="win-start-icon"></span>
        Start
    </a>
    <span class="win-taskbar-clock" id="win-clock" onclick="onClockClick()"></span>
</div>

<script>
    function onClockClick() {
        var f = document.createElement('form');
        f.method = 'POST';
        f.action = '/user/toggleAdmin';
        document.body.appendChild(f);
        f.submit();
    }

    function updateClock() {
        var now = new Date();
        var h = now.getHours();
        var m = now.getMinutes();
        var ampm = h >= 12 ? 'PM' : 'AM';
        h = h % 12 || 12;
        m = m < 10 ? '0' + m : m;
        document.getElementById('win-clock').textContent = h + ':' + m + ' ' + ampm;
    }
    updateClock();
    setInterval(updateClock, 30000);

    document.querySelectorAll('[data-utc]').forEach(function(el) {
        var d = new Date(parseInt(el.getAttribute('data-utc')));
        var yyyy = d.getFullYear();
        var mm = String(d.getMonth() + 1).padStart(2, '0');
        var dd = String(d.getDate()).padStart(2, '0');
        var hh = String(d.getHours()).padStart(2, '0');
        var min = String(d.getMinutes()).padStart(2, '0');
        el.textContent = mm + '-' + dd + '-' + yyyy + ' ' + hh + ':' + min;
    });
</script>

<g:if test="${session.userId}">
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2/lib/stomp.min.js"></script>
<script>
    (function() {
        var win = document.getElementById('chatWindow');
        var header = document.getElementById('chatTitlebar');
        var isDragging = false;
        var offsetX, offsetY;

        header.addEventListener('mousedown', function(e) {
            if (e.target.closest('.win-titlebar-btn')) return;
            if (window.innerWidth <= 768) return;
            isDragging = true;
            var rect = win.getBoundingClientRect();
            offsetX = e.clientX - rect.left;
            offsetY = e.clientY - rect.top;
            win.style.left = rect.left + 'px';
            win.style.top = rect.top + 'px';
            win.style.right = 'auto';
            win.style.bottom = 'auto';
            header.style.cursor = 'grabbing';
        });

        document.addEventListener('mousemove', function(e) {
            if (!isDragging) return;
            var x = e.clientX - offsetX;
            var y = e.clientY - offsetY;
            var rect = win.getBoundingClientRect();
            x = Math.max(0, Math.min(x, window.innerWidth - rect.width));
            y = Math.max(0, Math.min(y, window.innerHeight - rect.height));
            win.style.left = x + 'px';
            win.style.top = y + 'px';
        });

        document.addEventListener('mouseup', function() {
            isDragging = false;
            header.style.cursor = 'grab';
        });
    })();

    var stompClient = null;

    function escapeHtml(text) {
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function appendMessage(content, timestamp, nickname, color) {
        var el = document.getElementById('chatMessages');
        var d = new Date(timestamp);
        var mm = String(d.getMonth() + 1).padStart(2, '0');
        var dd = String(d.getDate()).padStart(2, '0');
        var yyyy = d.getFullYear();
        var hh = String(d.getHours()).padStart(2, '0');
        var min = String(d.getMinutes()).padStart(2, '0');
        var timeStr = mm + '-' + dd + '-' + yyyy + ' ' + hh + ':' + min;
        var nick = nickname || 'Anonymous';
        var col = color || '#CCCCCC';
        var div = document.createElement('div');
        div.className = 'win-chat-msg';
        div.title = timeStr;
        div.innerHTML = '<span class="win-chat-nick" style="color:' + col + '">' + escapeHtml(nick) + '</span>: '
            + escapeHtml(content);
        el.appendChild(div);
        el.scrollTop = el.scrollHeight;
    }

    function connectChat() {
        var socket = new SockJS('/ws/chat');
        stompClient = Stomp.over(socket);
        stompClient.debug = null;
        stompClient.connect({}, function() {
            stompClient.subscribe('/topic/chat', function(msg) {
                var data = JSON.parse(msg.body);
                appendMessage(data.content, data.timestamp, data.nickname, data.color);
            });
        }, function() {
            setTimeout(connectChat, 5000);
        });
    }

    function sendChatMessage() {
        var input = document.getElementById('chatInput');
        var text = input.value.trim();
        if (!text || !stompClient || !stompClient.connected) return;
        stompClient.send('/app/chat.send', {}, JSON.stringify({ content: text }));
        input.value = '';
    }

    function toggleChat() {
        var win = document.getElementById('chatWindow');
        var body = document.getElementById('chatBody');
        var btn = document.querySelector('#chatWindow .win-titlebar-btn');
        var collapsed = body.style.display !== 'none';
        body.style.display = collapsed ? 'none' : '';
        win.style.minHeight = collapsed ? '0' : '';
        win.style.height = collapsed ? 'auto' : '';
        win.style.resize = collapsed ? 'none' : '';
        btn.innerHTML = collapsed ? '+' : '&minus;';
        btn.title = collapsed ? 'Restore' : 'Minimize';
    }

    fetch('/chat/recent').then(function(r) { return r.json(); }).then(function(msgs) {
        msgs.forEach(function(m) { appendMessage(m.content, m.timestamp, m.nickname, m.color); });
    }).catch(function() {}).finally(function() { connectChat(); });

    // Load current user's nickname into placeholder
    fetch('/chat/myNickname').then(function(r) { return r.json(); }).then(function(data) {
        if (data.nickname) {
            document.getElementById('chatInput').placeholder = 'Type a message, ' + data.nickname + '...';
        }
    }).catch(function() {});
</script>
</g:if>

<div id="spinner" style="display:none;">Loading...</div>
<asset:javascript src="application.js"/>
<script>
if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/service-worker.js');
}
</script>
</body>
</html>
