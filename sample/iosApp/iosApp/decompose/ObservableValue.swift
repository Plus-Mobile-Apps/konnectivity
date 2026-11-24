//
//  ObservableValue.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 11/30/22.
//  Copyright © 2022 plus mobile apps. All rights reserved.
//

import shared

public class ObservableValue<T : AnyObject> : ObservableObject {
    @Published
    var value: T

    private var cancellation: Cancellation?

    init(_ value: Value<T>) {
        self.value = value.value
        self.cancellation = value.subscribe { [weak self] value in self?.value = value }
    }

    deinit {
        cancellation?.cancel()
    }
}
