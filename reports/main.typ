// Главный файл отчета
#import "title.typ": title_page

#set page(margin: 2.5cm)
#set figure(numbering: "1", supplement: [Рисунок])

// Титульная страница
#title_page(
  worktype: [По лабораторной работе 3],
  theme: [Вариант 11111],
  teacher: [Тюрин Иван Николаевич],
  author: [Алхимовици Арсений],
  group: [P3310],
  date: "2025",
)

#pagebreak()


#v(1cm)

#link("dabwish.ru")[https://dabwish.ru]

#figure(
  image("./wish-site/main-page.png", width: 100%),
  caption: [Главная страница разработанного приложения]
)
#figure(
  image("./wish-site/wish-page.png", width: 100%),
  caption: [Страница желания разработанного приложения]
)

#v(1cm)

= Исходный код

#link("https://github.com/senya-2011/wish-site")[https://github.com/senya-2011/wish-site]

#v(1cm)

= UML-диаграммы

#figure(
  image("wish-site-v2.png", width: 100%),
  caption: [UML-диаграмма архитектуры приложения]
)

#v(0.5cm)

#figure(
  image("./wish-site/wish-uml-classes.png", width: 100%),
  caption: [UML-диаграмма классов]
)

#v(1cm)

= Кеш-hits и Пул запросов Hikari

#figure(
  image("./grafana/grafana_hits.png", width: 100%),
  caption: [Диаграмма попаданий в кеш при запросах]
)

#v(0.5cm)

#figure(
  image("./redis/redis_vs_hikari_32users_1min_fixed.png", width: 100%),
  caption: [redis против hikari на 32 юзерах 1 минуту]
)

#v(0.5cm)

#figure(
  image("./grafana/hikari_connectionPool.png", width: 100%),
  caption: [Диаграмма пула подключений hikari]
)

#v(1cm)

= Выводы по работе

Реализовал подсистему массового импорта данных с обеспечением отказоустойчивости. Настроил транзакционное сохранение файлов в S3/MinIO с механизмом отката при сбоях.
Для повышения производительности внедрил L2-кэш Hibernate (Redis) для часто запрашиваемых сущностей и настроил аспектное (AOP) логирование метрик.