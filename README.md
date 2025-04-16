# 🚀 Explore With Me Next Version

**"Explore With Me"** — это современное микросервисное приложение для управления событиями, участия пользователей и персонализированных рекомендаций.  

![Architecture](https://img.shields.io/badge/Architecture-Microservices-blue) ![Java](https://img.shields.io/badge/Java-17%2B-orange) ![Kafka](https://img.shields.io/badge/Apache_Kafka-3.x-purple) ![gRPC](https://img.shields.io/badge/gRPC-1.5+-brightgreen)

---

## 🌟 Основные возможности
- **Управление событиями** (создание, модерация, фильтрация)  
- **Подписки и уведомления** о новых событиях  
- **Персонализированные рекомендации** на основе поведения пользователей  
- **Статистика в реальном времени** с использованием Kafka и gRPC  
- **Гибкая архитектура** (Discovery Server, Config Server, API Gateway)  

---

## 🏗 Архитектура
### Микросервисы
| Сервис                | Описание                                                                 |
|-----------------------|--------------------------------------------------------------------------|
| **События**           | Управление событиями, фильтрация, просмотры                             |
| **Подписки**          | Подписки на события/категории, уведомления                              |
| **Заявки**            | Подача и обработка заявок на участие                                    |
| **Пользователи**      | Регистрация, профили, управление аккаунтами                             |
| **Статистика** 📊     | Анализ данных, рекомендации (Kafka + gRPC)                              |

### Служебные сервисы
- **Discovery Server** — динамическое обнаружение сервисов  
- **Config Server** — централизованное управление конфигурациями  
- **Gateway** — единая точка входа для API  

---

## 🔍 Детали реализации
### Алгоритм рекомендаций
```java
public double get(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        return minWeightsSum.computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }
```

### Технологии
- **Backend**: Java 17, Spring Boot, Spring Cloud  
- **Базы данных**: PostgreSQL, H2 (для тестов)  
- **Брокер сообщений**: Apache Kafka  
- **gRPC**: Для высокоскоростного взаимодействия сервисов  
- **OpenAPI 3.0**: Документация API  

---

## 🛠 Установка и запуск
```bash
# 1. Клонировать репозиторий
git clone https://github.com/1EVILGUN1/java-ewm-next-version.git

# 2. Собрать проект
mvn clean package

# 3. Запустить Docker-контейнеры (Kafka, PostgreSQL, микросервисы)
docker-compose up -d

```

---

## 📈 Планы развития
- [ ] Добавить OAuth2 аутентификацию  
- [ ] Внедрить кэширование (Redis)  
- [ ] Улучшить рекомендации через ML  
- [ ] Поддержка мобильных push-уведомлений  
