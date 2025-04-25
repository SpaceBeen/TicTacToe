document.addEventListener('DOMContentLoaded', () => {
    const API_URL = 'http://localhost:8080/game';
    const AUTH_URL = 'http://localhost:8080/auth';
    const USER_URL = 'http://localhost:8080/user';
    let currentGameId = null;
    let userId = null;
    let playerSymbol = null;
    let pollingInterval = null;

    // DOM-элементы
    const cells = document.querySelectorAll('.cell');
    const newGameBtn = document.getElementById('newGameBtn');
    const availableGamesBtn = document.getElementById('availableGamesBtn');
    const joinGameBtn = document.getElementById('joinGameBtn');
    const userInfoBtn = document.getElementById('userInfoBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    const gameIdInput = document.getElementById('gameIdInput');
    const loginBtn = document.getElementById('loginBtn');
    const registerBtn = document.getElementById('registerBtn');
    const status = document.getElementById('status');
    const playerInfo = document.getElementById('player-info');
    const authStatus = document.getElementById('auth-status');
    const gameSection = document.getElementById('game-section');
    const authSection = document.getElementById('auth-section');
    const gameControls = document.getElementById('game-controls');
    const userLogin = document.getElementById('user-login');
    const userRating = document.getElementById('user-rating');
    const notification = document.getElementById('notification');
    const notificationText = document.getElementById('notification-text');
    const notificationClose = document.getElementById('notification-close');
    const loginInput = document.getElementById('login');
    const passwordInput = document.getElementById('password');
    const modeModal = document.getElementById('mode-modal');
    const humanModeBtn = document.getElementById('humanModeBtn');
    const computerModeBtn = document.getElementById('computerModeBtn');
    const modeCloseBtn = document.getElementById('modeCloseBtn');
    const availableGamesModal = document.getElementById('available-games-modal');
    const availableGamesList = document.getElementById('available-games-list');
    const availableGamesCloseBtn = document.getElementById('availableGamesCloseBtn');
    const userInfoModal = document.getElementById('user-info-modal');
    const userInfoContent = document.getElementById('user-info-content');
    const userInfoCloseBtn = document.getElementById('userInfoCloseBtn');
    const completedGamesBtn = document.getElementById('completedGamesBtn');
    const completedGamesModal = document.getElementById('completed-games-modal');
    const completedGamesList = document.getElementById('completed-games-list');
    const completedGamesCloseBtn = document.getElementById('completedGamesCloseBtn');
    const leaderboardBtn = document.getElementById('leaderboardBtn');
    const leaderboardCloseBtn = document.getElementById('leaderboardCloseBtn');



    // Получение заголовка авторизации
    function getAuthHeader() {
        const token = localStorage.getItem('accessToken');
        return token ? `Bearer ${token}` : null;
    }

    // Запуск polling для обновления состояния игры
    function startPolling() {
        if (pollingInterval) clearInterval(pollingInterval);
        pollingInterval = setInterval(fetchGameState, 2000);
        console.log(`Started polling for game ${currentGameId}`);
    }

    // Остановка polling
    function stopPolling() {
        if (pollingInterval) clearInterval(pollingInterval);
        pollingInterval = null;
        console.log(`Stopped polling for game ${currentGameId}`);
    }

    // Проверка авторизации
    async function checkAuth() {
        const token = localStorage.getItem('accessToken');
        if (token) {
            const userInfoFetched = await fetchUserInfo();
            if (userInfoFetched) {
                authSection.style.display = 'none';
                gameSection.style.display = 'block';
                gameControls.style.display = 'flex';
                status.textContent = 'Нажмите "Новая игра" для начала';
            } else {
                localStorage.removeItem('accessToken');
                authSection.style.display = 'block';
                gameSection.style.display = 'none';
                gameControls.style.display = 'none';
                userLogin.textContent = '';
                userRating.textContent = '';
            }
        } else {
            authSection.style.display = 'block';
            gameSection.style.display = 'none';
            gameControls.style.display = 'none';
            userLogin.textContent = '';
            userRating.textContent = '';
        }
    }

    // Отрисовка игрового поля
    function renderBoard(field) {
        if (!field || !Array.isArray(field) || field.length !== 3) {
            showNotification('Ошибка: неверное игровое поле');
            return;
        }
        cells.forEach(cell => {
            const x = parseInt(cell.dataset.x);
            const y = parseInt(cell.dataset.y);
            if (Array.isArray(field[x]) && field[x][y] !== undefined) {
                const value = field[x][y];
                cell.textContent = value === 1 ? 'X' : value === -1 ? 'O' : '';
            } else {
                cell.textContent = '';
            }
        });
    }

    // Отображение информации об игроках
    function renderPlayerInfo(symbols, mode) {
        if (!symbols || !symbols[userId]) {
            playerInfo.textContent = '';
            return;
        }
        if (mode === 'computer') {
            playerInfo.textContent = `Вы: ${symbols[userId] || 'X'}, Компьютер: ${symbols['computer'] || 'O'}`;
        } else {
            const opponentId = Object.keys(symbols).find(id => id && id !== userId && id !== 'computer' && id !== 'xPlayerId' && id !== 'oPlayerId');
            playerInfo.textContent = `Вы: ${symbols[userId] || 'X'}, Противник: ${opponentId ? symbols[opponentId] : 'ждём игрока'}`;
        }
    }

    // Показ уведомления
    function showNotification(message, duration = 3000) {
        notificationText.textContent = message;
        notification.classList.add('active');
        setTimeout(() => {
            notification.classList.remove('active');
        }, duration);
    }

    // Проверка окончания игры и управление клетками
    function checkGameEnd(gameState) {
        const { status: gameStatus, mode, symbols, playerId } = gameState;
        console.log('checkGameEnd:', { gameStatus, mode, playerId, userId, symbols });

        // Определяем символ текущего игрока
        let currentPlayerSymbol = null;
        if (playerId === symbols.xPlayerId) {
            currentPlayerSymbol = 'X';
        } else if (playerId === symbols.oPlayerId) {
            currentPlayerSymbol = 'O';
        }

        if (gameStatus.includes('Ничья') || gameStatus.includes('Победа')) {
            status.textContent = gameStatus;
            cells.forEach(cell => cell.classList.add('disabled'));
            currentGameId = null;
            playerSymbol = null;
            stopPolling();
            console.log('Game ended, polling stopped');
        } else if (gameStatus === 'Ожидание игроков') {
            status.textContent = 'Ожидание второго игрока';
            cells.forEach(cell => cell.classList.add('disabled'));
            startPolling();
            console.log('Waiting for players, polling started');
        } else {
            status.textContent = currentPlayerSymbol ? `Ход игрока ${currentPlayerSymbol}` : gameStatus;
            console.log(`Current turn: playerId=${playerId}, userId=${userId}, isUserTurn=${playerId === userId}`);

            // Активируем клетки, если ход текущего пользователя
            if (playerId === userId) {
                console.log('Enabling cells for user', userId);
                cells.forEach(cell => {
                    if (!cell.textContent) {
                        cell.classList.remove('disabled');
                    } else {
                        cell.classList.add('disabled');
                    }
                });
            } else {
                console.log('Disabling cells, not user turn');
                cells.forEach(cell => cell.classList.add('disabled'));
            }
            startPolling();
        }
        renderPlayerInfo(symbols, mode);
    }

    // Авторизация
    async function login(username, password) {
        try {
            const response = await fetch(`${AUTH_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ login: username, password })
            });
            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || 'Ошибка авторизации');
            }
            const data = await response.json();
            localStorage.setItem('accessToken', data.accessToken);
            authStatus.textContent = '';
            loginInput.value = '';
            passwordInput.value = '';
            await fetchUserInfo();
            authSection.style.display = 'none';
            gameSection.style.display = 'block';
            gameControls.style.display = 'flex';
            showNotification('Вход успешен!');
        } catch (error) {
            authStatus.textContent = `Ошибка: ${error.message}`;
            showNotification(`Ошибка: ${error.message}`);
        }
    }

    // Регистрация
    async function register(username, password) {
        try {
            const response = await fetch(`${AUTH_URL}/signup`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ login: username, password })
            });
            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || 'Ошибка регистрации');
            }
            showNotification('Регистрация успешна! Выполняется вход...');
            await login(username, password);
        } catch (error) {
            authStatus.textContent = `Ошибка: ${error.message}`;
            showNotification(`Ошибка: ${error.message}`);
        }
    }

    // Получение информации о пользователе
    async function fetchUserInfo() {
        try {
            const response = await fetch(`${USER_URL}/me`, {
                method: 'GET',
                headers: { 'Authorization': getAuthHeader() }
            });
            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || 'Не удалось получить данные пользователя');
            }
            const data = await response.json();
            userId = data.id;
            userLogin.textContent = data.login;
            userRating.textContent = `Рейтинг: ${data.rating || 0}`;
            userInfoContent.innerHTML = `
<p>ID: ${data.id}</p>
<p>Логин: ${data.login}</p>
<p>Рейтинг: ${data.rating}</p>
    `;
            if (data.currentGameId) {
                currentGameId = data.currentGameId;
                await joinGame(currentGameId);
            } else {
                status.textContent = 'Нажмите "Новая игра" для начала';
            }
            return true;
        } catch (error) {
            showNotification(`Ошибка: ${error.message}`);
            return false;
        }
    }

    // Создание новой игры
    async function startNewGame(mode) {
        if (!getAuthHeader()) {
            showNotification('Пожалуйста, авторизуйтесь!');
            authSection.style.display = 'block';
            gameSection.style.display = 'none';
            gameControls.style.display = 'none';
            return;
        }
        try {
            const response = await fetch(`${API_URL}/new`, {
                method: 'POST',
                headers: {
                    'Authorization': getAuthHeader(),
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ mode })
            });
            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || 'Не удалось создать игру');
            }
            const data = await response.json();
            if (!data.id) {
                throw new Error('Game ID not provided in response');
            }
            currentGameId = data.id;
            playerSymbol = data.xPlayer === userId ? 'X' : (data.oPlayer === userId ? 'O' : 'X');
            showNotification(`Игра создана! ID: ${data.id}, Режим: ${mode === 'human' ? 'с человеком' : 'с компьютером'}`);
            if (mode === 'computer') {
                await fetchGameState();
            } else {
                await joinGame(data.id);
            }
        } catch (error) {
            showNotification(`Ошибка создания игры: ${error.message}`);
        }
    }

    // Подключение к игре
    async function joinGame(gameId) {
        if (!getAuthHeader()) {
            showNotification('Пожалуйста, авторизуйтесь!');
            return;
        }
        const idToJoin = gameId || gameIdInput.value.trim();
        if (!idToJoin) {
            showNotification('Введите ID игры или создайте новую!');
            return;
        }
        try {
            const response = await fetch(`${API_URL}/${idToJoin}/join`, {
method: 'POST',
    headers: { 'Authorization': getAuthHeader() }
});
if (!response.ok) {
    const error = await response.json();
    if (error.error.includes('Пользователь уже участвует')) {
        currentGameId = idToJoin;
        await fetchGameState();
        showNotification(`Вы уже в игре ${idToJoin}`);
        startPolling();
        return;
    }
    throw new Error(error.error || 'Не удалось подключиться к игре');
}
const data = await response.json();
currentGameId = idToJoin;
playerSymbol = data.xPlayer === userId ? 'X' : (data.oPlayer === userId ? 'O' : 'X');
gameIdInput.value = '';
const gameBoard = data.gameBoard || [[0, 0, 0], [0, 0, 0], [0, 0, 0]];
renderBoard(gameBoard);
checkGameEnd({
    status: data.status || 'Ожидание игроков',
    mode: data.mode === 'Игра с человеком' ? 'human' : 'computer',
    symbols: {
        [data.xPlayer]: 'X',
        [data.oPlayer]: 'O',
        xPlayerId: data.xPlayer,
        oPlayerId: data.oPlayer
    },
    playerId: data.playerId
});
showNotification(`Подключено к игре ${idToJoin}`);
startPolling();
} catch (error) {
    showNotification(`Ошибка подключения: ${error.message}`);
}
}

// Получение состояния игры
async function fetchGameState() {
    if (!getAuthHeader() || !currentGameId) return;
    try {
        console.log(`Fetching game state for game ${currentGameId}`);
        const response = await fetch(`${API_URL}/${currentGameId}`, {
            method: 'GET',
            headers: { 'Authorization': getAuthHeader() }
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Не удалось получить состояние игры');
        }
        const data = await response.json();
        console.log('Game state received:', data);
        const gameBoard = data.gameBoard || [[0, 0, 0], [0, 0, 0], [0, 0, 0]];
        renderBoard(gameBoard);
        checkGameEnd({
            status: data.status || 'Ожидание хода',
            mode: data.mode === 'Игра с человеком' ? 'human' : 'computer',
            symbols: {
                [data.xPlayer]: 'X',
                [data.oPlayer]: 'O',
                xPlayerId: data.xPlayer,
                oPlayerId: data.oPlayer
            },
            playerId: data.playerId
        });
    } catch (error) {
        console.error('Fetch game state error:', error.message);
        showNotification(`Ошибка получения игры: ${error.message}`);
        stopPolling();
    }
}

// Ход в игре
async function makeMove(x, y) {
    if (!getAuthHeader() || !currentGameId) {
        showNotification('Подключитесь к игре!');
        return;
    }
    try {
        console.log(`Making move in game ${currentGameId} at [${x}, ${y}]`);
        stopPolling();
        const response = await fetch(`${API_URL}/${currentGameId}`, {
            method: 'POST',
            headers: {
                'Authorization': getAuthHeader(),
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ move: [x, y] })
        });
        const data = await response.json();
        console.log('Move response:', data);
        if (!response.ok) {
            throw new Error(data.error || 'Ошибка хода');
        }
        const gameBoard = data.gameBoard || [[0, 0, 0], [0, 0, 0], [0, 0, 0]];
        renderBoard(gameBoard);
        checkGameEnd({
            status: data.status || 'Ожидание хода',
            mode: data.mode === 'Игра с человеком' ? 'human' : 'computer',
            symbols: {
                [data.xPlayer]: 'X',
                [data.oPlayer]: 'O',
                xPlayerId: data.xPlayer,
                oPlayerId: data.oPlayer
            },
            playerId: data.playerId
        });
        console.log('Move successful, restarting polling');
        startPolling();
    } catch (error) {
        console.error('Make move error:', error.message);
        showNotification(`Ошибка хода: ${error.message}`);
        startPolling();
    }
}

// Получение доступных игр
async function fetchAvailableGames() {
    if (!getAuthHeader()) {
        showNotification('Пожалуйста, авторизуйтесь!');
        return;
    }
    try {
        const response = await fetch(`${API_URL}/available`, {
            method: 'GET',
            headers: { 'Authorization': getAuthHeader() }
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Не удалось получить доступные игры');
        }
        const data = await response.json();
        availableGamesList.innerHTML = '';
        if (data.length === 0) {
            availableGamesList.innerHTML = '<p>Нет доступных игр</p>';
        } else {
            data.forEach(game => {
                const gameItem = document.createElement('div');
                gameItem.className = 'game-item';
                gameItem.textContent = `Игра ${game.id}`;
                gameItem.addEventListener('click', () => {
                    availableGamesModal.classList.remove('active');
                    joinGame(game.id);
                });
                availableGamesList.appendChild(gameItem);
            });
        }
        availableGamesModal.classList.add('active');
    } catch (error) {
        showNotification(`Ошибка: ${error.message}`);
    }
}

// События
loginBtn.addEventListener('click', async () => {
    const username = loginInput.value.trim();
    const password = passwordInput.value.trim();
    if (!username || !password) {
        authStatus.textContent = 'Введите логин и пароль';
        return;
    }
    await login(username, password);
});

registerBtn.addEventListener('click', async () => {
    const username = loginInput.value.trim();
    const password = passwordInput.value.trim();
    if (!username || !password) {
        authStatus.textContent = 'Введите логин и пароль';
        return;
    }
    await register(username, password);
});
    async function fetchCompletedGames() {
        if (!getAuthHeader()) {
            showNotification('Пожалуйста, авторизуйтесь!');
            return;
        }
        try {
            const response = await fetch(`${USER_URL}/completedGames`, {
                method: 'GET',
                headers: { 'Authorization': getAuthHeader() }
            });
            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || 'Не удалось получить завершённые игры');
            }
            const data = await response.json();
            completedGamesList.innerHTML = '';
            if (data.length === 0) {
                completedGamesList.innerHTML = '<p>Нет завершённых игр</p>';
            } else {
                data.forEach(game => {
                    const gameItem = document.createElement('div');
                    gameItem.className = 'game-item';
                    gameItem.innerHTML = `
                    <p><strong>ID:</strong> ${game.id}</p>
                    <p><strong>Дата:</strong> ${game.dateOfCreation}</p>
                    <p><strong>Состояние:</strong> ${game.status}</p>
                    <p><strong>Победитель:</strong> ${game.playerId || 'Ничья'}</p>
                `;
                    completedGamesList.appendChild(gameItem);
                });
            }
            completedGamesModal.classList.add('active');
        } catch (error) {
            showNotification(`Ошибка: ${error.message}`);
        }
    }

    // Получение топа игроков
    async function fetchLeaderboard() {
        if (!getAuthHeader()) {
            showNotification('Пожалуйста, авторизуйтесь!');
            return;
        }
        try {
            const response = await fetch(`${USER_URL}/topList?limit=10`, {
                method: 'GET',
                headers: { 'Authorization': getAuthHeader() }
            });
            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || 'Не удалось получить топ игроков');
            }
            const data = await response.json();
            renderLeaderboard(data);
        } catch (error) {
            showNotification(`Ошибка: ${error.message}`);
        }
    }

// Отрисовка топа игроков
    function renderLeaderboard(players) {
        const leaderboardList = document.getElementById('leaderboard-list');
        leaderboardList.innerHTML = '';

        if (players.length === 0) {
            leaderboardList.innerHTML = '<p>Нет данных о игроках</p>';
            return;
        }

        // Создаем таблицу для красивого отображения
        const table = document.createElement('table');
        table.className = 'leaderboard-table';

        // Заголовок таблицы
        const headerRow = document.createElement('tr');
        headerRow.innerHTML = `
        <th>Место</th>
        <th>Игрок</th>
        <th>WinRate</th>
    `;
        table.appendChild(headerRow);

        // Данные игроков
        players.forEach((player, index) => {
            const row = document.createElement('tr');
            if (player.user_id === userId) {
                row.classList.add('current-user');
            }
            row.innerHTML = `
            <td>${index + 1}</td>
            <td>${player.login|| player.userId}</td>
            <td>${(player.winrate *100).toFixed(2)}%</td>
        `;
            table.appendChild(row);
        });

        leaderboardList.appendChild(table);
        document.getElementById('leaderboard-modal').classList.add('active');
    }

// События
    completedGamesBtn.addEventListener('click', fetchCompletedGames);
    completedGamesCloseBtn.addEventListener('click', () => {
        completedGamesModal.classList.remove('active');
    });

logoutBtn.addEventListener('click', () => {
    localStorage.removeItem('accessToken');
    currentGameId = null;
    userId = null;
    playerSymbol = null;
    stopPolling();
    authSection.style.display = 'block';
    gameSection.style.display = 'none';
    gameControls.style.display = 'none';
    userLogin.textContent = '';
    userRating.textContent = '';
    status.textContent = 'Нажмите "Новая игра" для начала';
    authStatus.textContent = '';
    renderBoard([[0, 0, 0], [0, 0, 0], [0, 0, 0]]);
    showNotification('Вы вышли из аккаунта');
});

newGameBtn.addEventListener('click', () => {
    modeModal.classList.add('active');
});

humanModeBtn.addEventListener('click', () => {
    modeModal.classList.remove('active');
    startNewGame('human');
});

computerModeBtn.addEventListener('click', () => {
    modeModal.classList.remove('active');
    startNewGame('computer');
});

modeCloseBtn.addEventListener('click', () => {
    modeModal.classList.remove('active');
});

availableGamesBtn.addEventListener('click', fetchAvailableGames);

availableGamesCloseBtn.addEventListener('click', () => {
    availableGamesModal.classList.remove('active');
});

joinGameBtn.addEventListener('click', () => joinGame());

userInfoBtn.addEventListener('click', () => {
    fetchUserInfo();
    userInfoModal.classList.add('active');
});

userInfoCloseBtn.addEventListener('click', () => {
    userInfoModal.classList.remove('active');
});

cells.forEach(cell => {
    cell.addEventListener('click', async () => {
        if (cell.classList.contains('disabled')) return;
        const x = parseInt(cell.dataset.x);
        const y = parseInt(cell.dataset.y);
        await makeMove(x, y);
    });
});

notificationClose.addEventListener('click', () => {
    notification.classList.remove('active');
});
    leaderboardBtn.addEventListener('click', fetchLeaderboard);
    leaderboardCloseBtn.addEventListener('click', () => {
        document.getElementById('leaderboard-modal').classList.remove('active');
    });


// Проверка авторизации при загрузке
checkAuth();
});