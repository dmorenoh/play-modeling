package example.videoclub.repository

import example.videoclub.services.AsyncResult


trait Repository[M[_]]

trait AsyncRepository extends Repository[AsyncResult]
