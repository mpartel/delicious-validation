# Delicious validation

A simple, no-nonsense Scala validation library.

## Example

Say we're building a webapp and want to validate a request to change one's password. Let's define the request object and its validators.

```scala
import so.delicious.validation._

case class ChangePassword(password: String, passwordAgain:  String) extends Validated {
  password ~ "must be at least 8 characters long" ~ (password.length > 8)

  password ~ "must contain both letters and numbers" ~ {
    password.exists(_.isLetter) && password.exists(_.isDigit)
  }

  passwordAgain ~ "does not match" when (passwordAgain != password)
}
```

Such request objects can be extracted from raw input with e.g. [json4s](https://github.com/json4s/json4s) or [salat](https://github.com/novus/salat).

```scala
val changePassword = json.extract[ChangePassword]
```

Now we can check which fields failed.

```scala
for (e <- changePassword.validationErrors) {
  e.context  // List[Symbol] - e.g. List('passwordAgain)
  e.message  // String       - e.g. "does not match"
  e.value    // Any
}

changePassword.isValid  // Shorthand for `validationErrors.isEmpty`
```

And that's all there is to it. ScalaDocs are available here [TBD], installation instructions and more advanced use cases are below.


## Installation

Scala 2.10.2+ is required.

There are no official releases just yet, but there will be soon.

[TBD: SBT and Maven instructions]


## Web frameworks

[TBD: for at least Scalatra and Play!]

### Client-side

[TBD: how to display errors]


## Advanced features

### Error message localization

Error messages are just plain strings in plain code, so you can pass them through any localization system directly.

```scala
// Imagine "string.tr()" is how you do localization
password ~ "must be at least %d characters".tr(8) ~ (password.length > 8)
```

### Recursive validation

[TBD. objects and collections]

### Custom field types

[TBD. PhoneNumber example]

### Reusable validation rules

[TBD]

### Maximum size annotations

[TBD]

### Dependency injection

[TBD]


## Rationale

- Validators should be written in plain code, not some [complicated DSL](http://eed3si9n.com/learning-scalaz/Validation.html) or [clunky annotation system](http://symfony.com/doc/current/book/validation.html).
- Error messages should be close to the validation code. Although we also support reusable validators, we believe a little potential repetition in the name of simplicity is a good tradeoff here.
- Validation errors should know the field that failed so the UI can display the error in the appropriate place.
- Validated objects should be composable. We validate fields recursively.
- Error message sentence structure should be free regulated.


## How it works

For those interested, there is a little bit of macro magic involved in capturing the names of the fields.

A "*field expression*" is either `this` or any expression that accesses a field or subfield. E.g. `[this.]address.zipCode` is a field expression but `a + b` is not.

An [implicit macro](http://docs.scala-lang.org/overviews/macros/implicits.html) converts any field expression to an object that carries the names of the fields in the field expression. The object has the '~(msg: String)' method, from which our little DSL continues like you'd expect. One awesome feature of implicit macros is that they let us give a clear error message when the expression before the tilde is not a field expression.


## Legal

Copyright (c) 2013, Martin Pärtel, [Solinor Oy](http://www.solinor.fi/)

License: 2-Clause BSD (see LICENSE.txt)

